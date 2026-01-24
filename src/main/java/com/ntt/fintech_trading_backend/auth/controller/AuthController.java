package com.ntt.fintech_trading_backend.auth.controller;

import com.ntt.fintech_trading_backend.auth.dto.request.CheckOtpRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.LoginRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.RegisterRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.SendOtpRequest;
import com.ntt.fintech_trading_backend.auth.dto.response.AuthResponse;
import com.ntt.fintech_trading_backend.auth.service.AuthService;
import com.ntt.fintech_trading_backend.common.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        return ResponseEntity.ok(authService.sendRegistrationOtp(request));
    }

    @PostMapping("/check-otp")
    public ResponseEntity<ApiResponse> checkOtp(@Valid @RequestBody CheckOtpRequest request) {
        return ResponseEntity.ok(authService.checkRegistrationOtp(request));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok(authService.refreshToken(authHeader));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}