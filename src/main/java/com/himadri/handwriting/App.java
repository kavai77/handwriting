package com.himadri.handwriting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import static com.himadri.handwriting.App.SECRET_STORE;

@SpringBootApplication
@PropertySource("file://" + SECRET_STORE + "secrets.yml")
public class App {
    public static final String SECRET_STORE = "/usr/share/secrets/handwriting-secrets/";

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
