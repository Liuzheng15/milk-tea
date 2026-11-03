package com.example.milktea;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.milktea.mapper")
public class MilkteaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilkteaApplication.class, args);
    }
}
