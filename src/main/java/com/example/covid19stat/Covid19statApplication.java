package com.example.covid19stat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
public class Covid19statApplication {

    public static void main(String[] args) {
        SpringApplication.run(Covid19statApplication.class, args);
    }

}
