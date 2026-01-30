package com.example.board.auth.config;

import com.example.board.auth.resolver.DeviceTypeResolver;
import com.example.board.auth.resolver.SignUpProofTokenResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addLast(new SignUpProofTokenResolver());
        resolvers.addLast(new DeviceTypeResolver());
    }

    @Override
    public void addViewControllers(org.springframework.web.servlet.config.annotation.ViewControllerRegistry registry) {
        registry.addViewController("/auth/login").setViewName("auth/login");
    }
}
