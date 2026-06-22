package com.github.leo51645.dailyfeed;

import com.github.leo51645.dailyfeed.service.NewsFetchCoordinator;
import com.github.leo51645.dailyfeed.service.YahooFinanceService;
import com.github.leo51645.dailyfeed.ui.ApiKeySetupDialog;
import com.github.leo51645.dailyfeed.ui.MainFrame;
import com.github.leo51645.dailyfeed.ui.SplashScreen;

import javax.swing.JOptionPane;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class DailyFeedApplication {

    public static void main(String[] args) throws Exception {

        if (!apiKeysConfigured()) {
            SwingUtilities.invokeAndWait(() -> {
                ApiKeySetupDialog dialog = new ApiKeySetupDialog();
                dialog.setVisible(true);
            });
        }

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
        NewsFetchCoordinator newsFetchCoordinator = context.getBean(NewsFetchCoordinator.class);

        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.showSplash();

            MainFrame mainFrame = new MainFrame(yahooFinanceService, newsFetchCoordinator, () -> {
                String[] existing = readExistingKeys();
                ApiKeySetupDialog dialog = new ApiKeySetupDialog(existing[0], existing[1]);
                dialog.setVisible(true);
                if (dialog.isCompleted() && dialog.keysChanged()) {
                    int choice = JOptionPane.showOptionDialog(null,
                            "API Keys gespeichert.\nDie App muss neu gestartet werden, damit die Änderungen wirksam werden.",
                            "Neustart erforderlich",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[]{"Jetzt neu starten", "Später"},
                            "Später");
                    if (choice == 0) {
                        System.exit(0);
                    }
                }
            });
            mainFrame.loadInitialData(splash);
        });
    }

    private static boolean apiKeysConfigured() {
        try {
            Path envPath = Path.of(".env");
            if (!Files.exists(envPath)) return false;
            String content = Files.readString(envPath);
            return content.contains("GEMINI_API_KEY=") && content.contains("CURRENTS_API_KEY=");
        } catch (IOException e) {
            return false;
        }
    }

    private static String[] readExistingKeys() {
        try {
            String content = Files.readString(Path.of(".env"));
            String gemini = extractValue(content, "GEMINI_API_KEY");
            String currents = extractValue(content, "CURRENTS_API_KEY");
            return new String[]{gemini, currents};
        } catch (IOException e) {
            return new String[]{"", ""};
        }
    }

    private static String extractValue(String content, String key) {
        for (String line : content.split("\n")) {
            if (line.startsWith(key + "=")) {
                return line.substring(key.length() + 1).trim();
            }
        }
        return "";
    }

}
