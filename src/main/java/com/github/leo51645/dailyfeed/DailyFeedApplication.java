package com.github.leo51645.dailyfeed;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DailyFeedApplication {

    public static void main(String[] args) {

        // Load environment variables from .env into system properties so Spring can access them
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String currentsApiKey = dotenv.get("CURRENTS_API_KEY");
        if (currentsApiKey != null) {
            System.setProperty("CURRENTS_API_KEY", currentsApiKey);
        }

        SpringApplication.run(DailyFeedApplication.class, args);

    }

}
