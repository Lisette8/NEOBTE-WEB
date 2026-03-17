package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.AI.ChatMessageDTO;
import com.sesame.neobte.DTO.Responses.Analytics.*;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeStatut;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Repositories.Fraude.IFraudeAlerteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.IVirementRepository;
import com.sesame.neobte.Services.Other.GroqAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalyticsServiceImpl implements AiAnalyticsService {

    private final IVirementRepository virementRepo;
    private final IUtilisateurRepository utilisateurRepo;
    private final IFraudeAlerteRepository alerteRepo;
    private final GroqAiService groqAiService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ─────────────────────────────────────────────────────────────────────────
    // ANALYTICS (no AI — pure aggregation)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponseDTO getAnalytics() {

        Date since30d = daysAgo(30);
        Date since12m = daysAgo(365);

        // Key metrics
        long   totalTransfers = virementRepo.countTotal();
        double totalVolume    = orZero(virementRepo.totalVolume());
        double avgTransfer    = orZero(virementRepo.avgTransfer());
        long   totalClients   = utilisateurRepo.countByRole(Role.CLIENT);
        long   openAlerts     = alerteRepo.countByStatut(FraudeStatut.OPEN);

        // Daily transfer stats (last 30 days)
        List<DailyStatDTO> dailyTransfers = parseDailyStats(
                virementRepo.dailyTransferStats(since30d));

        // 7-day forecast via linear regression on the last 30 days
        List<DailyStatDTO> forecast = buildForecast(dailyTransfers, 7);

        // Monthly user growth (last 12 months)
        List<MonthlyStatDTO> monthlyUsers = parseMonthlyStats(
                utilisateurRepo.monthlyUserGrowth(since12m));

        // Fraud by type / severity
        List<TypeStatDTO> fraudByType     = parseTypeStats(alerteRepo.countByType());
        List<TypeStatDTO> fraudBySeverity = parseTypeStats(alerteRepo.countBySeverity());

        // Fraud trend (last 30 days)
        List<DailyStatDTO> fraudTrend = parseDailyTrend(
                alerteRepo.dailyAlertTrend(since30d));

        // High-risk users
        List<UserRiskDTO> riskUsers = parseUserRisk(alerteRepo.userRiskSummary());

        return AnalyticsResponseDTO.builder()
                .totalTransfers(totalTransfers)
                .totalVolume(totalVolume)
                .avgTransfer(avgTransfer)
                .totalClients(totalClients)
                .openFraudAlerts(openAlerts)
                .dailyTransfers(dailyTransfers)
                .forecast(forecast)
                .monthlyUsers(monthlyUsers)
                .fraudByType(fraudByType)
                .fraudBySeverity(fraudBySeverity)
                .fraudTrend(fraudTrend)
                .highRiskUsers(riskUsers)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AI INSIGHTS (calls Groq)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public AiInsightsResponseDTO getAiInsights() {

        AnalyticsResponseDTO data = getAnalytics();

        String systemPrompt = """
                You are a senior financial analyst and fraud intelligence officer at NeoBTE, a Tunisian neobank.
                You are given aggregated transaction and fraud data. Respond concisely and professionally.
                Always respond in English. Keep each section under 120 words.
                """;

        // ── Fraud summary ──────────────────────────────────────────────────
        String fraudContext = buildFraudContext(data);
        String fraudSummary = groqAiService.chat(systemPrompt,
                "Based on this fraud data, write a concise fraud intelligence summary for the admin:\n\n" + fraudContext);

        // ── Financial insights ─────────────────────────────────────────────
        String finContext = buildFinancialContext(data);
        String financialInsights = groqAiService.chat(systemPrompt,
                "Based on this financial data, write a brief financial health assessment and trend analysis:\n\n" + finContext);

        // ── Recommendations ────────────────────────────────────────────────
        String recRaw = groqAiService.chat(systemPrompt,
                "Based on the following fraud and financial data, give exactly 4 short actionable recommendations " +
                "for the bank admin. Format: one recommendation per line, starting with a dash (-).\n\n" +
                fraudContext + "\n" + finContext);

        List<String> recommendations = Arrays.stream(recRaw.split("\n"))
                .map(String::trim)
                .filter(l -> l.startsWith("-"))
                .map(l -> l.substring(1).trim())
                .limit(4)
                .collect(Collectors.toList());

        if (recommendations.isEmpty()) {
            recommendations = List.of(recRaw.trim());
        }

        return AiInsightsResponseDTO.builder()
                .fraudSummary(fraudSummary)
                .financialInsights(financialInsights)
                .topRecommendations(recommendations)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // CHATBOT
    // ────────────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ChatResponseDTO chat(String message, List<ChatMessageDTO> history) {
        AnalyticsResponseDTO stats = getAnalytics();

        String systemPrompt = String.format("""
                You are NeoBTE's AI financial assistant for bank administrators.
                NeoBTE is a Tunisian digital neobank offering modern banking services.

                Current bank snapshot:
                - Total transfers: %d | Total volume: %.3f TND | Avg transfer: %.3f TND
                - Total clients: %d | Open fraud alerts: %d
                - Top fraud types: %s
                - High-risk users: %d flagged with open alerts

                Answer only questions related to finance, banking, fraud, user activity, and NeoBTE operations.
                Be concise, professional, and factual. Use the data above when relevant.
                Respond in English.
                """,
                stats.getTotalTransfers(), stats.getTotalVolume(), stats.getAvgTransfer(),
                stats.getTotalClients(), stats.getOpenFraudAlerts(),
                stats.getFraudByType().stream()
                        .map(t -> t.getLabel() + "(" + t.getCount() + ")")
                        .limit(3).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b),
                stats.getHighRiskUsers().size()
        );

        // Build full messages list: system + history + new user message
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            history.forEach(h -> messages.add(Map.of("role", h.getRole(), "content", h.getContent())));
        }
        messages.add(Map.of("role", "user", "content", message));

        String reply = groqAiService.chatWithHistory(messages);
        return new ChatResponseDTO(reply);
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Prompt builders
    // ─────────────────────────────────────────────────────────────────────────
    private String buildFraudContext(AnalyticsResponseDTO d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Open fraud alerts: ").append(d.getOpenFraudAlerts()).append("\n");
        sb.append("Alert breakdown by type: ");
        d.getFraudByType().forEach(t -> sb.append(t.getLabel()).append("=").append(t.getCount()).append(", "));
        sb.append("\nAlert breakdown by severity: ");
        d.getFraudBySeverity().forEach(t -> sb.append(t.getLabel()).append("=").append(t.getCount()).append(", "));
        sb.append("\nHigh-risk users (name, alertCount, highSeverityCount): ");
        d.getHighRiskUsers().stream().limit(5).forEach(u ->
                sb.append(u.getNom()).append("(").append(u.getAlertCount())
                  .append(" alerts, ").append(u.getHighSeverityCount()).append(" HIGH), "));
        return sb.toString();
    }

    private String buildFinancialContext(AnalyticsResponseDTO d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Total transfers: ").append(d.getTotalTransfers()).append("\n");
        sb.append("Total volume: ").append(String.format("%.3f TND", d.getTotalVolume())).append("\n");
        sb.append("Average transfer: ").append(String.format("%.3f TND", d.getAvgTransfer())).append("\n");
        sb.append("Total clients: ").append(d.getTotalClients()).append("\n");
        sb.append("Last 5 days transfer amounts: ");
        d.getDailyTransfers().stream()
                .skip(Math.max(0, d.getDailyTransfers().size() - 5))
                .forEach(s -> sb.append(s.getDate()).append("=")
                        .append(String.format("%.1f", s.getTotalAmount())).append("TND, "));
        sb.append("\n7-day forecast (predicted daily volume): ");
        d.getForecast().forEach(s -> sb.append(s.getDate()).append("=")
                .append(String.format("%.1f", s.getTotalAmount())).append("TND, "));
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data-mapping helpers
    // ─────────────────────────────────────────────────────────────────────────
    private List<DailyStatDTO> parseDailyStats(List<Object[]> rows) {
        return rows.stream().map(r -> {
            String date = r[0] == null ? "?" : r[0].toString().substring(0, 10);
            double amt  = r[1] == null ? 0 : ((Number) r[1]).doubleValue();
            long   cnt  = r[2] == null ? 0 : ((Number) r[2]).longValue();
            return new DailyStatDTO(date, amt, cnt);
        }).collect(Collectors.toList());
    }

    private List<DailyStatDTO> parseDailyTrend(List<Object[]> rows) {
        return rows.stream().map(r -> {
            String date = r[0] == null ? "?" : r[0].toString().substring(0, 10);
            long   cnt  = r[1] == null ? 0 : ((Number) r[1]).longValue();
            return new DailyStatDTO(date, 0, cnt);
        }).collect(Collectors.toList());
    }

    private List<MonthlyStatDTO> parseMonthlyStats(List<Object[]> rows) {
        return rows.stream().map(r -> {
            String month = r[0] == null ? "?" : r[0].toString();
            long   cnt   = r[1] == null ? 0 : ((Number) r[1]).longValue();
            return new MonthlyStatDTO(month, cnt);
        }).collect(Collectors.toList());
    }

    private List<TypeStatDTO> parseTypeStats(List<Object[]> rows) {
        return rows.stream().map(r -> {
            String label = r[0] == null ? "UNKNOWN" : r[0].toString();
            long   cnt   = r[1] == null ? 0 : ((Number) r[1]).longValue();
            return new TypeStatDTO(label, cnt);
        }).collect(Collectors.toList());
    }

    private List<UserRiskDTO> parseUserRisk(List<Object[]> rows) {
        return rows.stream().map(r -> {
            Long   id       = r[0] == null ? 0L : ((Number) r[0]).longValue();
            String prenom   = r[1] == null ? "" : r[1].toString();
            String nom      = r[2] == null ? "" : r[2].toString();
            String email    = r[3] == null ? "" : r[3].toString();
            long   total    = r[4] == null ? 0 : ((Number) r[4]).longValue();
            long   highCnt  = r[5] == null ? 0 : ((Number) r[5]).longValue();
            String level    = highCnt >= 2 ? "HIGH" : total >= 3 ? "MEDIUM" : "LOW";
            return new UserRiskDTO(id, prenom + " " + nom, email, total, highCnt, level);
        }).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7-day linear-regression forecast
    // ─────────────────────────────────────────────────────────────────────────
    private List<DailyStatDTO> buildForecast(List<DailyStatDTO> history, int days) {
        int n = history.size();
        if (n < 2) {
            // Not enough data — return flat forecast at 0
            return buildFlatForecast(days);
        }

        // Simple OLS linear regression: y = a + b*x  where x = day index
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = history.get(i).getTotalAmount();
            sumX  += x;
            sumY  += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double denom = n * sumX2 - sumX * sumX;
        double b = denom == 0 ? 0 : (n * sumXY - sumX * sumY) / denom;
        double a = (sumY - b * sumX) / n;

        List<DailyStatDTO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= days; i++) {
            double predicted = Math.max(0, a + b * (n + i - 1));
            result.add(new DailyStatDTO(today.plusDays(i).format(DATE_FMT), predicted, 0));
        }
        return result;
    }

    private List<DailyStatDTO> buildFlatForecast(int days) {
        List<DailyStatDTO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= days; i++) {
            result.add(new DailyStatDTO(today.plusDays(i).format(DATE_FMT), 0, 0));
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────────
    private Date daysAgo(int days) {
        return Date.from(LocalDate.now().minusDays(days)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private double orZero(Double d) {
        return d == null ? 0.0 : d;
    }
}
