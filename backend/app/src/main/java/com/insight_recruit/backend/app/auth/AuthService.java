package com.insight_recruit.backend.app.auth;

import com.insight_recruit.backend.app.auth.notification.OtpNotifier;
import com.insight_recruit.backend.app.auth.token.JwtTokenProvider;
import com.insight_recruit.backend.app.domain.entity.Tenant;
import com.insight_recruit.backend.app.domain.entity.TenantSubscription;
import com.insight_recruit.backend.app.domain.entity.User;
import com.insight_recruit.backend.app.domain.enums.PlanType;
import com.insight_recruit.backend.app.domain.enums.SubscriptionStatus;
import com.insight_recruit.backend.app.domain.enums.UserRole;
import com.insight_recruit.backend.app.dto.auth.AuthTokenResponse;
import com.insight_recruit.backend.app.repository.TenantSubscriptionRepository;
import com.insight_recruit.backend.app.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String GENERIC_REQUEST_OTP_MESSAGE =
        "If the identifier is registered, an OTP has been sent.";

    private final IdentifierNormalizer identifierNormalizer;
    private final OtpStoreService otpStoreService;
    private final OtpNotifier otpNotifier;
    private final UserRepository userRepository;
    private final TenantSubscriptionRepository tenantSubscriptionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.insight_recruit.backend.app.config.SecurityProperties securityProperties;

    public AuthService(
        IdentifierNormalizer identifierNormalizer,
        OtpStoreService otpStoreService,
        OtpNotifier otpNotifier,
        UserRepository userRepository,
        TenantSubscriptionRepository tenantSubscriptionRepository,
        JwtTokenProvider jwtTokenProvider,
        com.insight_recruit.backend.app.config.SecurityProperties securityProperties
    ) {
        this.identifierNormalizer = identifierNormalizer;
        this.otpStoreService = otpStoreService;
        this.otpNotifier = otpNotifier;
        this.userRepository = userRepository;
        this.tenantSubscriptionRepository = tenantSubscriptionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityProperties = securityProperties;
    }

    public String requestOtp(String rawIdentifier) {
        NormalizedIdentifier normalizedIdentifier = identifierNormalizer.normalize(rawIdentifier);
        otpStoreService.enforceRequestRateLimit(normalizedIdentifier.normalizedValue());

        Optional<User> userOpt = findUser(normalizedIdentifier);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            return GENERIC_REQUEST_OTP_MESSAGE;
        }

        String otp = otpStoreService.issueOtp(normalizedIdentifier.normalizedValue());
        otpNotifier.dispatch(normalizedIdentifier.type(), normalizedIdentifier.normalizedValue(), otp);
        return GENERIC_REQUEST_OTP_MESSAGE;
    }

    @Transactional
    public LoginResult verifyOtp(String rawIdentifier, String otpCode) {
        NormalizedIdentifier normalizedIdentifier = identifierNormalizer.normalize(rawIdentifier);
        boolean valid = otpStoreService.verifyAndConsumeOtp(normalizedIdentifier.normalizedValue(), otpCode);
        if (!valid) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        User user = findUser(normalizedIdentifier).orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
        if (!user.isActive()) {
            throw new InvalidOtpException("Account is inactive");
        }

        TenantSubscription subscription = tenantSubscriptionRepository.findById(user.getTenant().getId())
            .orElseGet(() -> createDefaultSubscription(user.getTenant()));

        String accessToken = jwtTokenProvider.generateAccessToken(user, subscription);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        ResponseCookie refreshCookie = ResponseCookie.from(securityProperties.refreshCookieName(), refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(securityProperties.refreshTokenTtlDays() * 24 * 60 * 60)
            .build();

        AuthTokenResponse tokenResponse = new AuthTokenResponse(
            accessToken,
            "Bearer",
            jwtTokenProvider.accessTokenExpiresInSeconds()
        );
        return new LoginResult(tokenResponse, refreshCookie.toString());
    }

    private TenantSubscription createDefaultSubscription(Tenant tenant) {
        TenantSubscription sub = TenantSubscription.builder()
            .tenant(tenant)
            .planType(PlanType.FREE)
            .status(SubscriptionStatus.ACTIVE)
            .featuresJson("{}")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        return tenantSubscriptionRepository.save(sub);
    }

    private Optional<User> findUser(NormalizedIdentifier identifier) {
        if (identifier.type() == AuthIdentifierType.EMAIL) {
            return userRepository.findByEmailIgnoreCase(identifier.normalizedValue());
        }
        return userRepository.findByPhoneNumber(identifier.normalizedValue());
    }

    public record LoginResult(AuthTokenResponse tokenResponse, String refreshCookieHeader) {}
}
