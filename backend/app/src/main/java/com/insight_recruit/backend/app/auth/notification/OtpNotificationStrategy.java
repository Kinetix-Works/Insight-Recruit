package com.insight_recruit.backend.app.auth.notification;

import com.insight_recruit.backend.app.auth.AuthIdentifierType;

public interface OtpNotificationStrategy {
    AuthIdentifierType supports();
    void send(String destination, String otpCode);
}
