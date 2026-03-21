export interface AuthResponse {
    token: string;
    pinRequired: boolean;
    pinTempToken: string;
}
