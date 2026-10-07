package org.fadhel.tumoohplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TumoohPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TumoohPlatformApplication.class, args);
    }

}
