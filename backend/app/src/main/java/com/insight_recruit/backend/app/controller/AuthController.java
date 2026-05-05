package com.insight_recruit.backend.app.controller;

import com.insight_recruit.backend.app.auth.AuthService;
import com.insight_recruit.backend.app.dto.auth.RequestOtpRequest;
import com.insight_recruit.backend.app.dto.auth.RequestOtpResponse;
import com.insight_recruit.backend.app.dto.auth.VerifyOtpRequest;
import jakarta.validation.Valid;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<RequestOtpResponse> requestOtp(@Valid @RequestBody RequestOtpRequest request) {
        String message = authService.requestOtp(request.identifier());
        return ResponseEntity.ok(new RequestOtpResponse(message));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthService.LoginResult loginResult = authService.verifyOtp(request.identifier(), request.otp());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookieHeader())
            .body(loginResult.tokenResponse());
    }
}
