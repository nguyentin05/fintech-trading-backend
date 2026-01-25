package com.ntt.fintech_trading_backend.auth.service;

import com.ntt.fintech_trading_backend.auth.domain.*;
import com.ntt.fintech_trading_backend.auth.dto.request.CheckOtpRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.LoginRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.RegisterRequest;
import com.ntt.fintech_trading_backend.auth.dto.request.SendOtpRequest;
import com.ntt.fintech_trading_backend.auth.dto.response.AuthResponse;
import com.ntt.fintech_trading_backend.auth.repository.TokenRepository;
import com.ntt.fintech_trading_backend.auth.repository.UserRepository;
import com.ntt.fintech_trading_backend.common.exception.AppException;
import com.ntt.fintech_trading_backend.common.exception.ErrorCode;
import com.ntt.fintech_trading_backend.infrastructure.security.JwtService;
import com.ntt.fintech_trading_backend.auth.security.SecurityUser;
import com.ntt.fintech_trading_backend.common.dto.response.ApiResponse;
import com.ntt.fintech_trading_backend.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private static final String OTP_PREFIX = "OTP_REG_";

    public ApiResponse<Void> sendRegistrationOtp(SendOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        redisTemplate.opsForValue().set(OTP_PREFIX + request.getEmail(), otp, 5, TimeUnit.MINUTES);
        emailService.sendOtpEmail(request.getEmail(), otp);

        return ApiResponse.<Void>builder().message("OTP đã được gửi thành công. Vui lòng kiểm tra email.").build();
    }

    public ApiResponse<Void> checkRegistrationOtp(CheckOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        String key = OTP_PREFIX + request.getEmail();
        String otp = redisTemplate.opsForValue().get(key);

        if (otp == null || !otp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        return ApiResponse.<Void>builder().message("OTP hợp lệ.").build();
    }

    @Transactional
    public ApiResponse<Void> register(RegisterRequest request) {
        String key = OTP_PREFIX + request.getEmail();
        String otp = redisTemplate.opsForValue().get(key);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (otp == null || !otp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
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

        return ApiResponse.<Void>builder().message("Đăng ký thành công.").build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        SecurityUser userDetails = new SecurityUser(user);

        var token = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        revokeAllUserTokens(user);
        saveUserToken(user, token);

        return AuthResponse.builder().accessToken(token).refreshToken(refreshToken).build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public AuthResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            if (jwtService.isTokenValid(refreshToken, new SecurityUser(user))) {
                var accessToken = jwtService.generateToken(new SecurityUser(user));

                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}