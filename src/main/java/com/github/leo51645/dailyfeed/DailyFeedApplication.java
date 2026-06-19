package com.github.leo51645.dailyfeed;

import com.github.leo51645.dailyfeed.service.GeminiService;
import com.github.leo51645.dailyfeed.service.YahooFinanceService;
import com.github.leo51645.dailyfeed.ui.MainFrame;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.swing.SwingUtilities;

@SpringBootApplication
public class DailyFeedApplication {

    public static void main(String[] args) {

        // Load environment variables from .env into system properties so Spring can access them
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String currentsApiKey = dotenv.get("CURRENTS_API_KEY");
        if (currentsApiKey != null) {
            System.setProperty("CURRENTS_API_KEY", currentsApiKey);
        }
        String geminiApiKey = dotenv.get("GEMINI_API_KEY");
        if (geminiApiKey != null) {
            System.setProperty("GEMINI_API_KEY", geminiApiKey);
        }

        // Spring Boot defaults java.awt.headless to true for web apps - the Swing UI needs it disabled.
        SpringApplication app = new SpringApplication(DailyFeedApplication.class);
        app.setHeadless(false);
        ApplicationContext context = app.run(args);

        YahooFinanceService yahooFinanceService = context.getBean(YahooFinanceService.class);
        GeminiService geminiService = context.getBean(GeminiService.class);

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(yahooFinanceService, geminiService);
            mainFrame.setVisible(true);
        });
    }

}
