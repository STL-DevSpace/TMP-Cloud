package org.dromara.projects;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication
public class ProjectsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectsApplication.class, args);
    }

}
