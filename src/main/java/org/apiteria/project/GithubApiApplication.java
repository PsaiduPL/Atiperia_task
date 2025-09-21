package org.apiteria.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Arrays;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableCaching
public class GithubApiApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext a =SpringApplication.run(GithubApiApplication.class, args);
        //Arrays.stream(a.getBeanDefinitionNames()).forEach(System.out::println);
    }

}
