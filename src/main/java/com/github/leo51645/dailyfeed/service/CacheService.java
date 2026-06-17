package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> loadSingleDay(LocalDate date) {
        String news;
        try {
            news = Files.readString(Paths.get(cacheUtil.getCacheURI(date.toString()).toUri()));
        } catch (IOException e) {
            throw new RuntimeException(e); // Todo: Error Handling
        }
        return geminiService.parseAIResponseToMap(news);
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> loadAllDays(LocalDate date) {
        String day1String;
        String day2String;
        String today;
        try {
            day1String = Files.readString(Paths.get(cacheUtil.getCacheURI(date.minusDays(2).toString()).toUri()));
            day2String = Files.readString(Paths.get(cacheUtil.getCacheURI(date.minusDays(1).toString()).toUri()));
            today = Files.readString(Paths.get(cacheUtil.getCacheURI(date.toString()).toUri()));
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: Exception Handling
        }
        String response = "[" + day1String + "," + day2String + "," + today + "]";
        return geminiService.parseAIResponseToMap(response);
    }
}
