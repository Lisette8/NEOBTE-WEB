package com.sesame.neobte.Services.Other;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesame.neobte.DTO.Responses.Market.CryptoRateDTO;
import com.sesame.neobte.DTO.Responses.Market.FxRateDTO;
import com.sesame.neobte.DTO.Responses.Market.MarketRatesResponseDTO;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MarketRatesService {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    
    public MarketRatesService(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
    }

    @Value("${market.cache-seconds:60}")
    private long cacheSeconds;

    @Value("${market.fx.usd-url:https://open.er-api.com/v6/latest/USD}")
    private String fxUsdUrl;

    @Value("${market.crypto.coingecko-url:https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,tether,binancecoin,solana&vs_currencies=usd,eur&precision=4}")
    private String cryptoUrl;

    private record CacheEntry(MarketRatesResponseDTO dto, long epochSeconds) {}
    private final AtomicReference<CacheEntry> cache = new AtomicReference<>();

    public MarketRatesResponseDTO getRates() {
        CacheEntry entry = cache.get();
        long now = Instant.now().getEpochSecond();
        if (entry != null && (now - entry.epochSeconds) < cacheSeconds) {
            return entry.dto;
        }

        try {
            MarketRatesResponseDTO fresh = fetchRates();
            cache.set(new CacheEntry(fresh, now));
            return fresh;
        } catch (Exception e) {
            if (entry != null) {
                MarketRatesResponseDTO stale = entry.dto;
                stale.setStale(true);
                return stale;
            }
            throw new BadRequestException("Impossible de récupérer les taux en ce moment. Réessayez plus tard.");
        }
    }

    private MarketRatesResponseDTO fetchRates() throws Exception {
        JsonNode fxRoot = getJson(fxUsdUrl);
        JsonNode ratesNode = fxRoot.path("rates");
        if (ratesNode.isMissingNode() || !ratesNode.isObject()) {
            throw new IllegalStateException("Invalid FX payload");
        }

        Map<String, Double> usdTo = new LinkedHashMap<>();
        ratesNode.fields().forEachRemaining(e -> {
            if (e.getValue().isNumber()) {
                usdTo.put(e.getKey().toUpperCase(), e.getValue().asDouble());
            }
        });

        Double usdToTnd = usdTo.get("TND");
        if (usdToTnd == null) throw new IllegalStateException("TND rate missing");

        List<FxRateDTO> fx = new ArrayList<>();
        fx.add(new FxRateDTO("USD/TND", round4(usdToTnd)));
        addTndCross(fx, usdTo, "EUR", usdToTnd);
        addTndCross(fx, usdTo, "GBP", usdToTnd);
        addTndCross(fx, usdTo, "AED", usdToTnd);
        addTndCross(fx, usdTo, "SAR", usdToTnd);
        addTndCross(fx, usdTo, "JPY", usdToTnd);

        JsonNode cryptoRoot = getJson(cryptoUrl);
        Map<String, String> idToSymbol = Map.of(
                "bitcoin", "BTC",
                "ethereum", "ETH",
                "tether", "USDT",
                "binancecoin", "BNB",
                "solana", "SOL"
        );

        List<CryptoRateDTO> crypto = new ArrayList<>();
        for (Map.Entry<String, String> e : idToSymbol.entrySet()) {
            JsonNode n = cryptoRoot.path(e.getKey());
            if (!n.isObject()) continue;
            Double usd = n.path("usd").isNumber() ? n.path("usd").asDouble() : null;
            Double eur = n.path("eur").isNumber() ? n.path("eur").asDouble() : null;
            Double tnd = usd != null ? round4(usd * usdToTnd) : null;
            crypto.add(new CryptoRateDTO(e.getValue(), usd != null ? round4(usd) : null, eur != null ? round4(eur) : null, tnd));
        }

        return new MarketRatesResponseDTO(
                Instant.now().toString(),
                false,
                round4(usdToTnd),
                fx,
                crypto
        );
    }

    private void addTndCross(List<FxRateDTO> fx, Map<String, Double> usdTo, String currency, double usdToTnd) {
        Double usdToCcy = usdTo.get(currency);
        if (usdToCcy == null || usdToCcy == 0) return;
        double ccyToTnd = usdToTnd / usdToCcy;
        fx.add(new FxRateDTO(currency + "/TND", round4(ccyToTnd)));
    }

    private JsonNode getJson(String url) throws Exception {
        String body = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
        if (body == null || body.isBlank()) throw new IllegalStateException("Empty response");
        return objectMapper.readTree(body);
    }

    private double round4(double v) {
        return Math.round(v * 10000d) / 10000d;
    }
}
