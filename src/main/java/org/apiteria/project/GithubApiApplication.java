package org.apiteria.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class GithubApiApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext a =SpringApplication.run(GithubApiApplication.class, args);
        //Arrays.stream(a.getBeanDefinitionNames()).forEach(System.out::println);
    }

}
