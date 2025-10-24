package com.study.jwtauth.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

/**
 * 구조화된 로깅을 위한 유틸리티 클래스
 * MDC(Mapped Diagnostic Context)를 사용하여 JSON 로그에 추가 정보를 포함
 */
public class StructuredLogger {

    /**
     * API 요청 로그 작성
     * requestId, clientIp는 MdcLoggingFilter에서 이미 설정됨
     */
    public static void logApiRequest(Logger logger, String method, String uri,
                                     Long userId, String userEmail, String userAgent) {
        try {
            // requestId, clientIp는 Filter에서 이미 설정되어 있음
            MDC.put("method", method);
            MDC.put("uri", uri);
            if (userId != null) {
                MDC.put("userId", userId.toString());
            }
            if (userEmail != null) {
                MDC.put("userEmail", userEmail);
            }
            if (userAgent != null) {
                MDC.put("userAgent", userAgent);
            }

            logger.info("API Request");
        } finally {
            // userId, userEmail은 response에서도 사용하므로 유지
            MDC.remove("userAgent");
        }
    }

    /**
     * API 응답 로그 작성
     */
    public static void logApiResponse(Logger logger, String method, String uri,
                                      Long userId, String userEmail,
                                      int status, long duration, String error) {
        try {
            MDC.put("status", String.valueOf(status));
            MDC.put("duration", String.valueOf(duration));
            if (error != null) {
                MDC.put("error", error);
                logger.error("API Response - Error");
            } else {
                logger.info("API Response");
            }
        } finally {
            // 모든 MDC 제거
            MDC.clear();
        }
    }

    /**
     * 인증 성공 로그 작성
     */
    public static void logAuthSuccess(Logger logger, String email, Long userId, String provider) {
        try {
            MDC.put("email", email);
            if (userId != null) {
                MDC.put("userId", userId.toString());
            }
            MDC.put("provider", provider != null ? provider : "local");
            MDC.put("result", "success");

            logger.info("Authentication successful");
        } finally {
            MDC.clear();
        }
    }

    /**
     * 인증 실패 로그 작성
     */
    public static void logAuthFailure(Logger logger, String email, String provider, String reason) {
        try {
            if (email != null) {
                MDC.put("email", email);
            }
            MDC.put("provider", provider != null ? provider : "local");
            MDC.put("result", "failure");
            MDC.put("reason", reason);

            logger.warn("Authentication failed");
        } finally {
            MDC.clear();
        }
    }

    /**
     * 로그아웃 로그 작성
     */
    public static void logLogout(Logger logger, String email, Long userId) {
        try {
            MDC.put("email", email);
            if (userId != null) {
                MDC.put("userId", userId.toString());
            }
            MDC.put("result", "success");

            logger.info("User logged out");
        } finally {
            MDC.clear();
        }
    }

    /**
     * 에러 로그 작성 (세부 정보만 포함)
     */
    public static void logError(Logger logger, String errorType, String errorCode,
                               Long userId, String userEmail, String requestUri,
                               String message, Throwable throwable) {
        try {
            MDC.put("errorType", errorType);
            if (errorCode != null) {
                MDC.put("errorCode", errorCode);
            }
            if (userId != null) {
                MDC.put("userId", userId.toString());
            }
            if (userEmail != null) {
                MDC.put("userEmail", userEmail);
            }
            if (requestUri != null) {
                MDC.put("requestUri", requestUri);
            }

            if (throwable != null) {
                logger.error(message, throwable);
            } else {
                logger.error(message);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * 비즈니스 예외 로그 작성
     */
    public static void logBusinessError(Logger logger, String errorCode, String message) {
        try {
            MDC.put("errorType", "BusinessException");
            MDC.put("errorCode", errorCode);

            logger.error(message);
        } finally {
            MDC.clear();
        }
    }

    /**
     * JWT 검증 실패 로그 작성
     */
    public static void logJwtValidationFailure(Logger logger, String requestUri, String errorType, String message) {
        try {
            if (requestUri != null) {
                MDC.put("requestUri", requestUri);
            }
            MDC.put("errorType", errorType);

            logger.error(message);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 인증/권한 예외 로그 작성
     */
    public static void logSecurityError(Logger logger, String requestUri, String errorType, String message) {
        try {
            if (requestUri != null) {
                MDC.put("requestUri", requestUri);
            }
            MDC.put("errorType", errorType);

            logger.error(message);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 커스텀 필드와 함께 로그 작성
     */
    public static void logWithContext(Logger logger, String message, Map<String, String> context) {
        try {
            if (context != null) {
                context.forEach(MDC::put);
            }
            logger.info(message);
        } finally {
            MDC.clear();
        }
    }
}
