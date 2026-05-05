package com.insight_recruit.backend.app.auth;

import com.insight_recruit.backend.app.auth.token.JwtTokenProvider;
import com.insight_recruit.backend.app.config.SecurityProperties;
import com.insight_recruit.backend.app.domain.entity.RefreshToken;
import com.insight_recruit.backend.app.domain.entity.Tenant;
import com.insight_recruit.backend.app.domain.entity.TenantSubscription;
import com.insight_recruit.backend.app.domain.entity.User;
import com.insight_recruit.backend.app.domain.enums.AuthProvider;
import com.insight_recruit.backend.app.domain.enums.PlanType;
import com.insight_recruit.backend.app.domain.enums.SubscriptionStatus;
import com.insight_recruit.backend.app.domain.enums.UserRole;
import com.insight_recruit.backend.app.dto.auth.AuthTokenResponse;
import com.insight_recruit.backend.app.dto.auth.AuthUserResponse;
import com.insight_recruit.backend.app.dto.auth.LoginRequest;
import com.insight_recruit.backend.app.dto.auth.OAuthLoginRequest;
import com.insight_recruit.backend.app.dto.auth.SignupInitiateRequest;
import com.insight_recruit.backend.app.dto.auth.SignupPasswordRequest;
import com.insight_recruit.backend.app.dto.auth.SignupVerifyRequest;
import com.insight_recruit.backend.app.repository.RefreshTokenRepository;
import com.insight_recruit.backend.app.repository.TenantRepository;
import com.insight_recruit.backend.app.repository.TenantSubscriptionRepository;
import com.insight_recruit.backend.app.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final OtpStoreService otpStoreService;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantSubscriptionRepository tenantSubscriptionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    public AuthService(
        OtpStoreService otpStoreService,
        UserRepository userRepository,
        TenantRepository tenantRepository,
        TenantSubscriptionRepository tenantSubscriptionRepository,
        RefreshTokenRepository refreshTokenRepository,
        JwtTokenProvider jwtTokenProvider,
        PasswordEncoder passwordEncoder,
        SecurityProperties securityProperties
    ) {
        this.otpStoreService = otpStoreService;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantSubscriptionRepository = tenantSubscriptionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }

    @Transactional
    public String initiateSignup(SignupInitiateRequest request) {
        String email = normalizeEmail(request.email());
        otpStoreService.enforceRequestRateLimit(email);

        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> createLocalUser(request, email));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(request.role());
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String otp = otpStoreService.issueOtp(email);
        log.info("Dispatching signup OTP to {} (code={})", email, otp);
        return "OTP sent. Please verify your email.";
    }

    @Transactional
    public String verifySignupOtp(SignupVerifyRequest request) {
        String email = normalizeEmail(request.email());
        boolean valid = otpStoreService.verifyAndConsumeOtp(email, request.otp());
        if (!valid) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return "Email verified successfully.";
    }

    @Transactional
    public LoginResult setPassword(SignupPasswordRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("No account exists for this email"));
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Email must be verified before setting password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email first");
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return issueTokens(user);
    }

    @Transactional
    public LoginResult oauthLogin(OAuthLoginRequest request) {
        if (request.provider() == AuthProvider.LOCAL) {
            throw new IllegalArgumentException("OAuth provider must be GOOGLE, MICROSOFT, or ZOHO");
        }
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            user = createOAuthUser(request, email);
        } else if (user.getAuthProvider() == AuthProvider.LOCAL && user.getPasswordHash() != null) {
            user.setAuthProvider(request.provider());
        }
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public LoginResult refresh(String refreshToken) {
        try {
            var claims = jwtTokenProvider.parseClaims(refreshToken);
            if (!"refresh".equals(claims.get("tokenType", String.class))) {
                throw new IllegalArgumentException("Invalid refresh token");
            }
            String tokenId = claims.getId();
            if (tokenId == null) {
                throw new IllegalArgumentException("Invalid refresh token");
            }
            RefreshToken stored = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token revoked"));
            if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Refresh token expired");
            }
            return issueTokens(stored.getUser());
        } catch (JwtException ex) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            String tokenId = jwtTokenProvider.parseClaims(refreshToken).getId();
            if (tokenId == null) {
                return;
            }
            refreshTokenRepository.findByTokenId(tokenId).ifPresent(token -> {
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            });
        } catch (JwtException ignored) {
        }
    }

    private LoginResult issueTokens(User user) {
        TenantSubscription subscription = tenantSubscriptionRepository.findById(user.getTenant().getId())
            .orElseGet(() -> createDefaultSubscription(user.getTenant()));
        String refreshTokenId = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.generateAccessToken(user, subscription);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, refreshTokenId);

        refreshTokenRepository.save(RefreshToken.builder()
            .user(user)
            .tokenId(refreshTokenId)
            .expiresAt(LocalDateTime.now().plusDays(securityProperties.refreshTokenTtlDays()))
            .createdAt(LocalDateTime.now())
            .build());

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
            jwtTokenProvider.accessTokenExpiresInSeconds(),
            toUserResponse(user)
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

    private User createLocalUser(SignupInitiateRequest request, String email) {
        Tenant tenant = createTenantFromEmail(email);
        return User.builder()
            .tenant(tenant)
            .email(email)
            .firstName(request.firstName().trim())
            .lastName(request.lastName().trim())
            .role(request.role())
            .authProvider(AuthProvider.LOCAL)
            .isEmailVerified(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private User createOAuthUser(OAuthLoginRequest request, String email) {
        Tenant tenant = createTenantFromEmail(email);
        return User.builder()
            .tenant(tenant)
            .email(email)
            .firstName(request.firstName().trim())
            .lastName(request.lastName().trim())
            .role(UserRole.RECRUITER)
            .authProvider(request.provider())
            .isEmailVerified(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private Tenant createTenantFromEmail(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        return tenantRepository.save(Tenant.builder()
            .companyName(domain)
            .apiKey(UUID.randomUUID().toString())
            .createdAt(LocalDateTime.now())
            .build());
    }

    private String normalizeEmail(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return rawEmail.trim().toLowerCase(Locale.ROOT);
    }

    private AuthUserResponse toUserResponse(User user) {
        return new AuthUserResponse(
            user.getId().toString(),
            user.getTenant().getId().toString(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name(),
            user.getAuthProvider().name()
        );
    }

    public record LoginResult(AuthTokenResponse tokenResponse, String refreshCookieHeader) {}
}
