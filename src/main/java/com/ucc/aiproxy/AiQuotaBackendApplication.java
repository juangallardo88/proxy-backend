package com.ucc.aiproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiQuotaBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiQuotaBackendApplication.class, args);
    }
}
