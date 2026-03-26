import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Investment, InvestmentCreateDTO, InvestmentPlan, InvestmentPlanFormDTO } from '../Entities/Interfaces/investment';

@Injectable({ providedIn: 'root' })
export class InvestmentService {

    private clientApi = 'http://localhost:8080/api/v1/client/investments';
    private adminApi = 'http://localhost:8080/api/v1/admin/investments';

    constructor(private http: HttpClient) { }

    // Client
    getActivePlans(): Observable<InvestmentPlan[]> {
        return this.http.get<InvestmentPlan[]>(`${this.clientApi}/plans`);
    }
    subscribe(dto: InvestmentCreateDTO): Observable<Investment> {
        return this.http.post<Investment>(`${this.clientApi}/subscribe`, dto);
    }
    cancel(id: number): Observable<Investment> {
        return this.http.post<Investment>(`${this.clientApi}/${id}/cancel`, {});
    }
    getMyInvestments(): Observable<Investment[]> {
        return this.http.get<Investment[]>(`${this.clientApi}/my`);
    }

    // Admin
    getAllPlans(): Observable<InvestmentPlan[]> {
        return this.http.get<InvestmentPlan[]>(`${this.adminApi}/plans`);
    }
    createPlan(dto: InvestmentPlanFormDTO): Observable<InvestmentPlan> {
        return this.http.post<InvestmentPlan>(`${this.adminApi}/plans`, dto);
    }
    updatePlan(id: number, dto: InvestmentPlanFormDTO): Observable<InvestmentPlan> {
        return this.http.put<InvestmentPlan>(`${this.adminApi}/plans/${id}`, dto);
    }
    deletePlan(id: number): Observable<any> {
        return this.http.delete(`${this.adminApi}/plans/${id}`);
    }
    getAllInvestments(): Observable<Investment[]> {
        return this.http.get<Investment[]>(`${this.adminApi}/all`);
    }
}