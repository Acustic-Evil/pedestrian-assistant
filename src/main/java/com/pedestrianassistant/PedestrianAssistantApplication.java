package com.pedestrianassistant;

import  org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class PedestrianAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedestrianAssistantApplication.class, args);
    }

}
