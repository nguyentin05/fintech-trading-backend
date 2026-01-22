package com.ntt.fintech_trading_backend.auth.controller;

import com.ntt.fintech_trading_backend.auth.service.AuthService;
import com.ntt.fintech_trading_backend.common.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(@RequestBody Map<String, String> request) {
        authService.sendRegistrationOtp(request.get("email"));

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("OTP đã được gửi thành công. Vui lòng kiểm tra email.")
                        .build()
        );
    }
}