package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsFetchCoordinator {

    private final CacheService cacheService;
    private final GeminiService geminiService;

    // gets cached news and missing entries and combines them into one HashMap for the frontend
    private Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> getMissingNews(LocalDate today, Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> cachedMap) {
        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> missingNews = new HashMap<>();
        if (!cachedMap.containsKey(today) && !cachedMap.containsKey(today.minusDays(1))) {
            missingNews =  geminiService.getNewsAllDays(today);
        } else if (!cachedMap.containsKey(today)) {
            missingNews =  geminiService.getNewsTwoDays(today);
        } else if (cachedMap.containsKey(today)) {
            missingNews =  geminiService.getNewsOneDay(today);
        }
        return missingNews;
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> combineCachedMissingNews (LocalDate today) {
        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> cachedNews = cacheService.loadAllCachedDays(today);
        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> missingNews = getMissingNews(today, cachedNews);
        cachedNews.putAll(missingNews);
        return cachedNews;
    }
}
