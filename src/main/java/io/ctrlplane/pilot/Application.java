package io.ctrlplane.pilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/** The application entry point. */
@SpringBootApplication
@EnableCaching
public class Application {

    /**
     * Main method.
     *
     * @param args The application arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
