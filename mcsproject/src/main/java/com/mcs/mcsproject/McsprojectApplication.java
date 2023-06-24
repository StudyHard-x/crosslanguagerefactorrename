package com.mcs.mcsproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.oas.annotations.EnableOpenApi;

@EnableOpenApi
@SpringBootApplication
public class McsprojectApplication {

    public static void main(String[] args) {
        SpringApplication.run(McsprojectApplication.class, args);
    }

}
