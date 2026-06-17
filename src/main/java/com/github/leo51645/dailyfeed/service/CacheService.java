package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class CacheService {

    private final GeminiService geminiService;
    private final CacheUtil cacheUtil;

    public void save(Map<String, JSONObject> aiResponse) {
        try {
            Files.createDirectories(Paths.get("cache/"));
            for (Map.Entry<String, JSONObject> entry : aiResponse.entrySet()) {
                Files.writeString(Paths.get(cacheUtil.getCacheURI(entry.getKey()).toUri()), entry.getValue().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: Exception Handling
        }
    }

    public void delete(LocalDate date) {
        try {
            Files.delete(cacheUtil.getCacheURI(date.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: Error handling
        }
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> loadSingleCachedDay(LocalDate date) {
        String news;
        try {
            news = Files.readString(cacheUtil.getCacheURI(date.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e); // Todo: Error Handling
        }
        return geminiService.parseAIResponseToMap(news);
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> loadAllCachedDays(LocalDate today) {
        String day1CachedString;
        String day2CachedString;
        String todayCachedString;
        try {
            day1CachedString = Files.readString(cacheUtil.getCacheURI(today.minusDays(2).toString()));
            day2CachedString = Files.readString(cacheUtil.getCacheURI(today.minusDays(1).toString()));
            todayCachedString = Files.readString(cacheUtil.getCacheURI(today.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: Exception Handling
        }
        String response = "[" + day1CachedString + "," + day2CachedString + "," + todayCachedString + "]";

        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> cachedNews = geminiService.parseAIResponseToMap(response);

        for (Map.Entry<LocalDate, Map<News_Categories, List<NewsResponseDto>>> entry : cachedNews.entrySet()) {
            LocalDate keyDate = entry.getKey();
            if (keyDate.isBefore(today.minusDays(2))) {
                delete(keyDate);
                cachedNews.remove(keyDate);
            }
        }
        return cachedNews;
    }
}
