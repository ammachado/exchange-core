package org.openpredict.exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableConfigurationProperties
@PropertySource("application.properties")
public class ExchangeCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeCoreApplication.class, args);
    }
}
