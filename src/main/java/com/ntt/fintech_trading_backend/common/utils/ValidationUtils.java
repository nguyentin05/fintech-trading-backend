package com.ntt.fintech_trading_backend.common.utils;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ValidationUtils {
    public String mapFieldToVietnamese(String fieldName) {
        return switch (fieldName) {
            case "email" -> "Email";
            case "otp" -> "Mã OTP";
            case "firstName" -> "Tên";
            case "lastName" -> "Họ";
            case "password" -> "Mật khẩu";
            case "phoneNumber" -> "Số điện thoại";
            case "dob" -> "Ngày sinh";
            default -> fieldName;
        };
    }

    public String mapAttribute(String message, Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            if (key.equals("groups") || key.equals("payload") || key.equals("message")) continue;
            String value = String.valueOf(entry.getValue());
            message = message.replace("{" + key + "}", value);
        }
        if (attributes.containsKey("value")) {
            String val = String.valueOf(attributes.get("value"));
            message = message.replace("{min}", val);
            message = message.replace("{max}", val);
        }
        return message;
    }
}