package com.insight_recruit.backend.app.auth;

import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class IdentifierNormalizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{7,14}$");

    public NormalizedIdentifier normalize(String rawIdentifier) {
        if (rawIdentifier == null || rawIdentifier.isBlank()) {
            throw new IllegalArgumentException("Identifier is required");
        }

        String value = rawIdentifier.trim();
        if (EMAIL_PATTERN.matcher(value).matches()) {
            return new NormalizedIdentifier(AuthIdentifierType.EMAIL, value.toLowerCase(Locale.ROOT));
        }

        String compact = value.replaceAll("\\s+", "");
        if (PHONE_PATTERN.matcher(compact).matches()) {
            return new NormalizedIdentifier(AuthIdentifierType.PHONE, compact);
        }

        throw new IllegalArgumentException("Identifier must be a valid email or phone number");
    }
}
