package com.walkmission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WalkMissionApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalkMissionApplication.class, args);
    }
}
