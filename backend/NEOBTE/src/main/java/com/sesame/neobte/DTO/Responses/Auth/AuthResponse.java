package com.sesame.neobte.DTO.Responses.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {
    /** Full JWT — present when authentication is complete. */
    private String token;

    /** True when the user has PIN enabled and must verify it before getting a full JWT. */
    private boolean pinRequired;

    /** Short-lived token identifying the half-authenticated session (PIN pending). */
    private String pinTempToken;
}
