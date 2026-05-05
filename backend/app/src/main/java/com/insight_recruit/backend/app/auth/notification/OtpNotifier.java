package com.insight_recruit.backend.app.auth.notification;

import com.insight_recruit.backend.app.auth.AuthIdentifierType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OtpNotifier {

    private final Map<AuthIdentifierType, OtpNotificationStrategy> strategies = new EnumMap<>(AuthIdentifierType.class);

    public OtpNotifier(List<OtpNotificationStrategy> strategyImplementations) {
        for (OtpNotificationStrategy strategy : strategyImplementations) {
            strategies.put(strategy.supports(), strategy);
        }
    }

    public void dispatch(AuthIdentifierType type, String destination, String otpCode) {
        OtpNotificationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("No OTP strategy configured for " + type);
        }
        strategy.send(destination, otpCode);
    }
}
