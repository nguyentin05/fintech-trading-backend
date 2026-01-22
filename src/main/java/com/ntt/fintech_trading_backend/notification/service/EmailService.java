package com.ntt.fintech_trading_backend.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Fintech Support <support@fintech.com>");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            javaMailSender.send(message);
            log.info("Email đã gửi thành công tới: {}", to);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email tới {}: {}", to, e.getMessage());
        }
    }

    public void sendOtpEmail(String to, String otp) {
        try {
            Context context = new Context();
            context.setVariable("email", to);
            context.setVariable("otp", otp);

            String body = templateEngine.process("email/otp-email", context);

            this.sendEmail(to, "Mã xác thực đăng ký tài khoản - Fintech Trading", body);

        } catch (Exception e) {
            log.error("Lỗi khi render template email: {}", e.getMessage());
        }
    }
}
