package com.caresync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CareSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareSyncApplication.class, args);
        System.out.println(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("Admin@123"));
    }
}
