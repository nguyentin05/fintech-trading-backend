package com.ntt.fintech_trading_backend.auth.service;

import com.ntt.fintech_trading_backend.auth.domain.User;
import com.ntt.fintech_trading_backend.auth.domain.UserRole;
import com.ntt.fintech_trading_backend.auth.domain.UserStatus;
import com.ntt.fintech_trading_backend.auth.dto.request.CheckOtpRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.RegisterRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.SendOtpRequest;
import com.ntt.fintech_trading_backend.auth.repository.UserRepository;
import com.ntt.fintech_trading_backend.common.dto.response.ApiResponse;
import com.ntt.fintech_trading_backend.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse sendRegistrationOtp(SendOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        redisTemplate.opsForValue().set("OTP_REG_" + request.getEmail(), otp, 5, TimeUnit.MINUTES);
        emailService.sendOtpEmail(request.getEmail(), otp);

        return ApiResponse.builder().message("OTP đã được gửi thành công. Vui lòng kiểm tra email.").build();
    }

    public ApiResponse checkRegistrationOtp(CheckOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        String key = "OTP_REG_" + request.getEmail();
        String otp = redisTemplate.opsForValue().get(key);

        if (otp == null) {
            throw new RuntimeException("Mã OTP đã hết hạn hoặc không tồn tại.");
        }

        if (!otp.equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không chính xác.");
        }

        return ApiResponse.builder().message("OTP hợp lệ.").build();
    }

    public ApiResponse register(RegisterRequest request) {
        String key = "OTP_REG_" + request.getEmail();
        String otp = redisTemplate.opsForValue().get(key);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        if (otp == null || !otp.equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không hợp lệ.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.TRADER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        redisTemplate.delete(key);

        return ApiResponse.builder().message("Đăng ký thành công.").build();
    }
}