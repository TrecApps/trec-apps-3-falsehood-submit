package com.trecapps.falsehoods.submit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.trecapps.base.FalsehoodModel","com.trecapps.base.InfoResource"})
public class Driver {

    public static void main(String[] args) {
        SpringApplication.run(Driver.class, args);
    }
}
