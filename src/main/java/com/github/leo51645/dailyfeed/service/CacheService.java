package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void deleteOldCacheFiles() {
        LocalDate today = LocalDate.now();
        Path cacheDir = Paths.get("cache/");
        if (!Files.exists(cacheDir)) {
            return;
        }
        try {
            Files.list(cacheDir)
                    .filter(path -> path.getFileName().toString().matches("day_\\d{4}-\\d{2}-\\d{2}\\.json"))
                    .forEach(path -> {
                        try {
                            LocalDate fileDate = LocalDate.parse(path.getFileName().toString().replace("day_", "").replace(".json", ""));
                            if (fileDate.isBefore(today.minusDays(2))) {
                                Files.delete(path);
                                log.info("Deleted old cache file: {}", path.getFileName());
                            }
                        } catch (Exception e) {
                            log.warn("Could not delete old cache file {}, skipping", path.getFileName(), e);
                        }
                    });
        } catch (IOException e) {
            log.warn("Could not list cache directory for cleanup", e);
        }
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> loadAllCachedDays(LocalDate today) {
        List<String> existingCachedDays = new ArrayList<>();

        if (Files.exists(cacheUtil.getCacheURI(today.minusDays(2).toString()))) {
            try {
                existingCachedDays.add(Files.readString(cacheUtil.getCacheURI(today.minusDays(2).toString())));
            } catch (IOException e) {
                throw new RuntimeException(e); // Todo: Exception Handling
            }
        }
        if (Files.exists(cacheUtil.getCacheURI(today.minusDays(1).toString()))) {
            try {
                existingCachedDays.add(Files.readString(cacheUtil.getCacheURI(today.minusDays(1).toString())));
            } catch (IOException e) {
                throw new RuntimeException(e); // Todo: Exception Handling
            }
        }
        if (Files.exists(cacheUtil.getCacheURI(today.toString()))) {
            try {
                existingCachedDays.add(Files.readString(cacheUtil.getCacheURI(today.toString())));
            } catch (IOException e) {
                throw new RuntimeException(e); // Todo: Exception Handling
            }
        }
        if (existingCachedDays.isEmpty()) {
            return new HashMap<>();
        }

        String combinedFiles = String.join(",", existingCachedDays);
        String response = "[" + combinedFiles + "]";

        return geminiService.parseAIResponseToMap(response);
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
