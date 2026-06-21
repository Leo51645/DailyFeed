package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.swing.text.DateFormatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    public void saveLastFetch(LocalDateTime lastFetch) {
        try {
            Files.createDirectories(Paths.get("cache/"));
            Files.writeString(Paths.get("cache/lastFetch.txt"), lastFetch.toString());
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: Exception handling
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
        String newsCached;
        try {
            newsCached = Files.readString(cacheUtil.getCacheURI(date.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e); // Todo: Error Handling
        }

        String news = "[" + newsCached + "]";
        return geminiService.parseAIResponseToMap(news);
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> loadAllCachedDays(LocalDate today) {
        String day1CachedString;
        String day2CachedString;
        String todayCachedString;

        List<String> existingCachedDays = new ArrayList<>();

        List<LocalDate> oldCacheFiles =  new ArrayList<>();

        if (Files.exists(cacheUtil.getCacheURI(today.minusDays(2).toString()))) {
            try {
                day1CachedString = Files.readString(cacheUtil.getCacheURI(today.minusDays(2).toString()));
                existingCachedDays.add(day1CachedString);
            } catch (IOException e) {
                throw new RuntimeException(e); // Todo: Exception Handling
            }
        }
        if (Files.exists(cacheUtil.getCacheURI(today.minusDays(1).toString()))) {
            try {
                day2CachedString = Files.readString(cacheUtil.getCacheURI(today.minusDays(1).toString()));
                existingCachedDays.add(day2CachedString);
            } catch (IOException e) {
                throw new RuntimeException(e); // Todo: Exception Handling
            }
        }
        if (Files.exists(cacheUtil.getCacheURI(today.toString()))) {
            try {
                todayCachedString = Files.readString(cacheUtil.getCacheURI(today.toString()));
                existingCachedDays.add(todayCachedString);
            } catch (IOException e) {
                throw new RuntimeException(e); // Todo: Exception Handling
            }
        }
        if (existingCachedDays.isEmpty()) {
            return new HashMap<>();
        }

        String cominedFiles = String.join(",", existingCachedDays);
        String response = "[" + cominedFiles + "]";

        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> cachedNews = geminiService.parseAIResponseToMap(response);

        for (Map.Entry<LocalDate, Map<News_Categories, List<NewsResponseDto>>> entry : cachedNews.entrySet()) {
            LocalDate keyDate = entry.getKey();
            if (keyDate.isBefore(today.minusDays(2))) {
                try {
                    delete(keyDate);
                } catch (RuntimeException e) {
                    log.warn("Could not delete old cache file for {}, skipping", keyDate, e);
                }
                oldCacheFiles.add(keyDate);
            }
        }

        for (LocalDate keyDate : oldCacheFiles) {
            cachedNews.remove(keyDate);
        }

        return cachedNews;
    }

    public LocalDateTime loadLastFetch() {
        Path lastFetchPath = Paths.get("cache/lastFetch.txt");

        String lastFetchString;
        try {
            lastFetchString = Files.readString(lastFetchPath);
        } catch (IOException e) {
            if (!Files.exists(lastFetchPath)) {
                return null;
            } else {
                throw new RuntimeException(e); // TODO: Exception Handling
            }
        }
        return LocalDateTime.parse(lastFetchString);
    }
}
