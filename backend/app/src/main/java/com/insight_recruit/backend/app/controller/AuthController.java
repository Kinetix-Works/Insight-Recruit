package com.insight_recruit.backend.app.controller;

import com.insight_recruit.backend.app.auth.AuthService;
import com.insight_recruit.backend.app.config.SecurityProperties;
import com.insight_recruit.backend.app.dto.auth.LoginRequest;
import com.insight_recruit.backend.app.dto.auth.OAuthLoginRequest;
import com.insight_recruit.backend.app.dto.auth.SignupInitiateRequest;
import com.insight_recruit.backend.app.dto.auth.SignupPasswordRequest;
import com.insight_recruit.backend.app.dto.auth.SignupVerifyRequest;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityProperties securityProperties;

    public AuthController(AuthService authService, SecurityProperties securityProperties) {
        this.authService = authService;
        this.securityProperties = securityProperties;
    }

    @PostMapping("/signup/initiate")
    public ResponseEntity<Map<String, String>> signupInitiate(@Valid @RequestBody SignupInitiateRequest request) {
        String message = authService.initiateSignup(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<Map<String, String>> signupVerify(@Valid @RequestBody SignupVerifyRequest request) {
        String message = authService.verifySignupOtp(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/signup/password")
    public ResponseEntity<?> signupPassword(@Valid @RequestBody SignupPasswordRequest request) {
        AuthService.LoginResult loginResult = authService.setPassword(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookieHeader())
            .body(loginResult.tokenResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult loginResult = authService.login(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookieHeader())
            .body(loginResult.tokenResponse());
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<?> oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        AuthService.LoginResult loginResult = authService.oauthLogin(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookieHeader())
            .body(loginResult.tokenResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String refreshToken = readRefreshCookie(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token missing");
        }
        AuthService.LoginResult loginResult = authService.refresh(refreshToken);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookieHeader())
            .body(loginResult.tokenResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String refreshToken = readRefreshCookie(request);
        authService.logout(refreshToken);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, securityProperties.refreshCookieName() + "=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Strict")
            .body(Map.of("message", "Logged out"));
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (securityProperties.refreshCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
