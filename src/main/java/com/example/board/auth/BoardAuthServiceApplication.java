package com.example.board.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class BoardAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardAuthServiceApplication.class, args);
    }

}
