package com.example.board.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth ->
                auth
                        .requestMatchers(
                                "/error",
                                "/auth/signup",
                                "/auth/login",
                                "/auth/members/availability/**",
                                "/auth/email-verifications/**")
                        .permitAll()
                        .anyRequest().authenticated());
        http.formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login/process")
                .permitAll());

        return http.build();
    }
}
