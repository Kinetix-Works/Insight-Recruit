package com.insight_recruit.backend.app.auth.notification;

import com.insight_recruit.backend.app.auth.AuthIdentifierType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailOtpNotificationStrategy implements OtpNotificationStrategy {
    @Override
    public AuthIdentifierType supports() {
        return AuthIdentifierType.EMAIL;
    }

    @Override
    public void send(String destination, String otpCode) {
        log.info("Dispatching email OTP to {} (code={})", destination, otpCode);
    }
}
