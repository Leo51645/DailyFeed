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
                Path path = cacheUtil.getCacheURI(entry.getKey());
                Files.writeString(path, entry.getValue().toString());
                log.info("Saved cache file: {}", path.getFileName());
            }
        } catch (IOException e) {
            log.error("Failed to write cache files", e);
            throw new RuntimeException("Cache write failed: " + e.getMessage(), e);
        }
    }

    public void saveLastFetch(LocalDateTime lastFetch) {
        try {
            Files.createDirectories(Paths.get("cache/"));
            Files.writeString(Paths.get("cache/lastFetch.txt"), lastFetch.toString());
            log.debug("Last fetch timestamp saved: {}", lastFetch);
        } catch (IOException e) {
            log.warn("Failed to save last fetch timestamp — refresh interval tracking may be inaccurate", e);
        }
    }

    public void delete(LocalDate date) {
        try {
            Files.delete(cacheUtil.getCacheURI(date.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> loadSingleCachedDay(LocalDate date) {
        Path path = cacheUtil.getCacheURI(date.toString());
        try {
            String newsCached = Files.readString(path);
            log.info("Loaded cache for date: {}", date);
            return geminiService.parseAIResponseToMap("[" + newsCached + "]");
        } catch (IOException e) {
            log.error("Failed to read cache file for date {}", date, e);
            throw new RuntimeException("Cache read failed for date " + date + ": " + e.getMessage(), e);
        }
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

        for (int i = 2; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Path path = cacheUtil.getCacheURI(date.toString());
            if (Files.exists(path)) {
                try {
                    existingCachedDays.add(Files.readString(path));
                    log.debug("Loaded cache for {}", date);
                } catch (IOException e) {
                    log.warn("Failed to read cache for {} — skipping this day", date, e);
                }
            }
        }

        if (existingCachedDays.isEmpty()) {
            return new HashMap<>();
        }

        String response = "[" + String.join(",", existingCachedDays) + "]";
        return geminiService.parseAIResponseToMap(response);
    }

    public LocalDateTime loadLastFetch() {
        Path lastFetchPath = Paths.get("cache/lastFetch.txt");

        if (!Files.exists(lastFetchPath)) {
            log.debug("No lastFetch.txt found — treating as first run");
            return null;
        }

        try {
            LocalDateTime lastFetch = LocalDateTime.parse(Files.readString(lastFetchPath));
            log.debug("Last fetch loaded: {}", lastFetch);
            return lastFetch;
        } catch (IOException e) {
            log.warn("Failed to read lastFetch.txt — treating as first run", e);
            return null;
        }
    }
}
