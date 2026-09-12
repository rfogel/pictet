package com.pictet;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class App {

    static void main() {
        new SpringApplicationBuilder(App.class).run();
    }
}
