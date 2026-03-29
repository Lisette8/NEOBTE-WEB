import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Loan, LoanProduct, LoanProductFormDTO, LoanRequestDTO, LoanSimulation } from '../Entities/Interfaces/loan';

@Injectable({ providedIn: 'root' })
export class LoanService {
    private clientApi = 'http://localhost:8080/api/v1/client/loans';
    private adminApi = 'http://localhost:8080/api/v1/admin/loans';
    constructor(private http: HttpClient) { }

    // Client
    getProducts(): Observable<LoanProduct[]> {
        return this.http.get<LoanProduct[]>(`${this.clientApi}/products`);
    }
    simulate(montant: number, tauxAnnuel: number, dureeEnMois: number): Observable<LoanSimulation> {
        return this.http.get<LoanSimulation>(`${this.clientApi}/simulate`, {
            params: { montant, tauxAnnuel, dureeEnMois }
        });
    }
    requestLoan(dto: LoanRequestDTO): Observable<Loan> {
        return this.http.post<Loan>(`${this.clientApi}/request`, dto);
    }
    getMyLoans(): Observable<Loan[]> {
        return this.http.get<Loan[]>(`${this.clientApi}/my`);
    }

    // Admin
    getAllProducts(): Observable<LoanProduct[]> {
        return this.http.get<LoanProduct[]>(`${this.adminApi}/products`);
    }
    createProduct(dto: LoanProductFormDTO): Observable<LoanProduct> {
        return this.http.post<LoanProduct>(`${this.adminApi}/products`, dto);
    }
    updateProduct(id: number, dto: LoanProductFormDTO): Observable<LoanProduct> {
        return this.http.put<LoanProduct>(`${this.adminApi}/products/${id}`, dto);
    }
    deleteProduct(id: number): Observable<any> {
        return this.http.delete(`${this.adminApi}/products/${id}`);
    }
    getAllLoans(): Observable<Loan[]> {
        return this.http.get<Loan[]>(`${this.adminApi}/all`);
    }
    approveLoan(id: number, adminNote: string): Observable<Loan> {
        return this.http.put<Loan>(`${this.adminApi}/${id}/approve`, { adminNote });
    }
    rejectLoan(id: number, motif: string): Observable<Loan> {
        return this.http.put<Loan>(`${this.adminApi}/${id}/reject`, { motif });
    }
}