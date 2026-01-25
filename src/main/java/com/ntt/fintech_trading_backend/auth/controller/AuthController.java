package com.ntt.fintech_trading_backend.auth.controller;

import com.ntt.fintech_trading_backend.auth.dto.request.CheckOtpRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.LoginRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.RegisterRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.SendOtpRequest;
import com.ntt.fintech_trading_backend.auth.dto.response.AuthResponse;
import com.ntt.fintech_trading_backend.auth.service.AuthService;
import com.ntt.fintech_trading_backend.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthService authService;

    @PostMapping("/send-otp")
    ApiResponse<Void> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        return authService.sendRegistrationOtp(request);
    }

    @PostMapping("/check-otp")
    ApiResponse<Void> checkOtp(@Valid @RequestBody CheckOtpRequest request) {
        return authService.checkRegistrationOtp(request);
    }

    @PostMapping("/register")
    ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(request);
        return ApiResponse.<AuthResponse>builder().result(result).build();
    }

    @PostMapping("/refresh-token")
    ApiResponse<AuthResponse> refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        var result = authService.refreshToken(authHeader);
        return ApiResponse.<AuthResponse>builder().result(result).build();
    }
}