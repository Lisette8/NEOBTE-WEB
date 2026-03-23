import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";


export interface ReferralEntry {
    id: number;
    referredName: string;
    referredEmail: string;
    dateReferral: string;
    rewarded: boolean;
}

export interface ReferralDashboard {
    referralCode: string;
    referralLink: string;
    totalReferrals: number;
    premium: boolean;
    premiumExpiresAt: string | null;
    referrals: ReferralEntry[];
}

@Injectable({ providedIn: 'root' })
export class ReferralService {

    private api = 'http://localhost:8080/api/v1/client/referral';

    constructor(private http: HttpClient) { }

    getDashboard(): Observable<ReferralDashboard> {
        return this.http.get<ReferralDashboard>(this.api);
    }

    applyCode(referralCode: string): Observable<{ message: string }> {
        return this.http.post<{ message: string }>(`${this.api}/apply-code`, { referralCode });
    }
}