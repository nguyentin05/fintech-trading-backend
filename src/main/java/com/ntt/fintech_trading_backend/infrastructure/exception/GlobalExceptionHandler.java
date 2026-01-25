package com.ntt.fintech_trading_backend.infrastructure.exception;


import com.ntt.fintech_trading_backend.common.dto.response.ApiResponse;
import com.ntt.fintech_trading_backend.common.exception.AppException;
import com.ntt.fintech_trading_backend.common.exception.ErrorCode;
import com.ntt.fintech_trading_backend.common.utils.ValidationUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ValidationUtils validationUtils;

    // authentication & security
    @ExceptionHandler(value = ExpiredJwtException.class)
    ResponseEntity<ApiResponse<?>> handlingExpiredJwtException(ExpiredJwtException exception) {
        return buildResponse(ErrorCode.EXPIRED_JWT);
    }

    @ExceptionHandler(value = JwtException.class)
    ResponseEntity<ApiResponse<?>> handlingInvalidJwtException(JwtException exception) {
        return buildResponse(ErrorCode.INVALID_JWT);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(AccessDeniedException exception) {
        return buildResponse(ErrorCode.ACCESS_DENIED);
    }

    // validation & input data

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handlingValidationException(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolation = exception.getBindingResult()
                    .getAllErrors().getFirst()
                    .unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();

        } catch (IllegalArgumentException e) {

        }

        String message = errorCode.getMessage();
        String fieldName = exception.getFieldError().getField();
        String vietnameseFieldName = validationUtils.mapFieldToVietnamese(fieldName);

        message = message.replace("{field}", vietnameseFieldName);

        if (Objects.nonNull(attributes)) {
            message = validationUtils.mapAttribute(message, attributes);
        }

        return buildResponse(errorCode, message);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<?>> handlingJsonException(HttpMessageNotReadableException exception) {
        return buildResponse(ErrorCode.INVALID_JSON);
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<?>> handlingTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        String message = ErrorCode.TYPE_MISMATCH.getMessage().replace("{field}", exception.getName());
        return buildResponse(ErrorCode.TYPE_MISMATCH, message);
    }

    // custom & database
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        return buildResponse(exception.getErrorCode());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<?>> handlingDbException(DataIntegrityViolationException exception) {
        return buildResponse(ErrorCode.DB_CONSTRAINT_VIOLATION);
    }

    // url

    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<ApiResponse<?>> handlingNotFoundException(NoResourceFoundException exception) {
        return buildResponse(ErrorCode.NOT_FOUND_URL);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiResponse<?>> handlingMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return buildResponse(ErrorCode.INVALID_METHOD_URL);
    }

    // server

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<?>> handlingException(Exception exception) {
        return buildResponse(ErrorCode.UNCATEGORIZED);
    }

    private ResponseEntity<ApiResponse<?>> buildResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    private ResponseEntity<ApiResponse<?>> buildResponse(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(message)
                        .build());
    }
}