package com.sesame.neobte.Services;

public interface PasswordResetService {
    void sendResetCode(String email);
    String verifyCode(String email, String code);
    void resetPassword(String resetToken, String newPassword);
}
