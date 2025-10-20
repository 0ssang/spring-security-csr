package com.study.jwtauth.presentataion.interceptor;

import com.study.jwtauth.presentataion.dto.common.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * ApiResponse를 반환하는 모든 응답에 대해 HTTP 상태 코드를 자동으로 설정
 */
@RestControllerAdvice
public class ResponseInterceptor implements ResponseBodyAdvice<Object> {

    /**
     * ApiResponse 타입의 응답에만 적용
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().equals(ApiResponse.class);
    }

    /**
     * 응답 본문이 작성되기 전에 HTTP 상태 코드 설정
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                   MethodParameter returnType,
                                   MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request,
                                   ServerHttpResponse response) {

        if (body instanceof ApiResponse<?> apiResponse) {
            response.setStatusCode(apiResponse.httpStatus());
        }

        return body;
    }
}
