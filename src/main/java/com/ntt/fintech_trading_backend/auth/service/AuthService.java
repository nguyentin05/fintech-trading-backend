package com.ntt.fintech_trading_backend.auth.service;

import com.ntt.fintech_trading_backend.auth.dto.request.SendOtpRequest;
import com.ntt.fintech_trading_backend.auth.repository.UserRepository;
import com.ntt.fintech_trading_backend.common.dto.response.ApiResponse;
import com.ntt.fintech_trading_backend.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    public ApiResponse sendRegistrationOtp(SendOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        redisTemplate.opsForValue().set("OTP_REG_" + request.getEmail(), otp, 5, TimeUnit.MINUTES);
        emailService.sendOtpEmail(request.getEmail(), otp);

        return ApiResponse.builder().message("OTP đã được gửi thành công. Vui lòng kiểm tra email.").build();
    }
}