package com.example.board.auth.resolver;

import com.example.board.auth.annotation.DeviceType;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class DeviceTypeResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(DeviceType.class) &&
                parameter.getParameterType().equals(String.class);
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        return request.getHeader("X-Device-Type");
    }
}
