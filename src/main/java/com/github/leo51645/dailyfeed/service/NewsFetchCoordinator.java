package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsFetchCoordinator {

    private final CacheService cacheService;
    private final GeminiService geminiService;

    private String getMissingNews(LocalDate today, Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> cachedMap) {
        String missingNews = "";

        // if today and yesterday aren't cached, get first news of the two first days and afterward of the latest day
        if (!cachedMap.containsKey(today) && !cachedMap.containsKey(today.minusDays(1))) {
            missingNews = geminiService.getNewsTwoDays(today);
            cacheService.saveLastFetch(LocalDateTime.now());

            // today-2 in the background afterward
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    String day1Response = geminiService.getNewsOneDay(today.minusDays(2));
                    cacheService.save(geminiService.parseAiResponseToJsonObject(day1Response));
                    log.info("today-2 ({}) cached afterward", today.minusDays(2));
                } catch (Exception e) {
                    log.error("Exception at loading today-2", e);
                }
            });

            // if only today is not cached get news from today and yesterday
        } else if (!cachedMap.containsKey(today)) {
            missingNews = geminiService.getNewsTwoDays(today);
            cacheService.saveLastFetch(LocalDateTime.now());

            // if today is cached and the lastFetch is older than 30 min get news of today
        } else if (cachedMap.containsKey(today)) {
            LocalDateTime lastFetch = cacheService.loadLastFetch();
            if (lastFetch == null || lastFetch.plusMinutes(30).isBefore(LocalDateTime.now())) {
                missingNews = geminiService.getNewsOneDay(today);
                cacheService.saveLastFetch(LocalDateTime.now());
            }
        }
        return missingNews;
    }

    // gets cached news and missing entries and combines them into one HashMap for the frontend
    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> combineCachedMissingNews (LocalDate today) {
        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> cachedNews = cacheService.loadAllCachedDays(today);
        String missingNewsAiResponse = getMissingNews(today, cachedNews);

        if (missingNewsAiResponse == null || missingNewsAiResponse.isEmpty()) {
            return cachedNews;
        }

        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> missingNews = geminiService.parseAIResponseToMap(missingNewsAiResponse);

        Map<String, JSONObject> missingNewsJson = geminiService.parseAiResponseToJsonObject(missingNewsAiResponse);
        cacheService.save(missingNewsJson);

        cachedNews.putAll(missingNews);
        return cachedNews;
    }
}
