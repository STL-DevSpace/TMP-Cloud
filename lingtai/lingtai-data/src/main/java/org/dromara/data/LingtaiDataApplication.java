package org.dromara.data;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableDubbo
@SpringBootApplication
@EnableAsync
public class LingtaiDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingtaiDataApplication.class, args);
    }

}
