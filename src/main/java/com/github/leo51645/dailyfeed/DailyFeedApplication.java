package com.github.leo51645.dailyfeed;

import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.domain.dto.response.NewsResponseDto;
import com.github.leo51645.dailyfeed.service.GeminiService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

        ApplicationContext context = SpringApplication.run(DailyFeedApplication.class, args);

        GeminiService geminiService = context.getBean(GeminiService.class);
        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> result = geminiService.getNewsAllDays(LocalDate.now());

        for (Map.Entry<LocalDate, Map<News_Categories, List<NewsResponseDto>>> dayEntry : result.entrySet()) {
            System.out.println("\n=== " + dayEntry.getKey() + " ===");
            for (Map.Entry<News_Categories, List<NewsResponseDto>> categoryEntry : dayEntry.getValue().entrySet()) {
                System.out.println("  [" + categoryEntry.getKey() + "]");
                for (NewsResponseDto article : categoryEntry.getValue()) {
                    System.out.println("    - " + article.getTitle());
                }
            }
        }

    }

}
