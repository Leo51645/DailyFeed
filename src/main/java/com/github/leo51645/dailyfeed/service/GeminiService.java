package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.response.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CurrentsNewsUtil;
import com.github.leo51645.dailyfeed.util.GeminiServiceUtil;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private Client client;
    private final GeminiServiceUtil geminiServiceUtil;
    private final CurrentsNewsUtil currentsNewsUtil;
    private final CurrentsNewsService currentsNewsService;

    @PostConstruct
    private void init() {
        client = Client.builder().apiKey(apiKey).build();
    }

    // create AI prompt out off all news, send it and get response of the gemini AI
    private GenerateContentResponse getGeminiNewsResponseAllDays(List<NewsResponseDto> firstDay, List<NewsResponseDto> secondDay, List<NewsResponseDto> today) {
        return client.models.generateContent("gemini-3.5-flash",
                "You are a news ranking assistant. You will receive news articles from three different days, each containing multiple categories.\n" +
                "\n" +
                "Your task:\n" +
                "1. For each day and each category, select the 5 most important and relevant articles\n" +
                "2. Base your ranking on: relevance, significance of the event, and informational value\n" +
                "3. Remove duplicate articles (same story, different sources) — keep only the most informative version\n" +
                "4. Convert all publishedAt timestamps to exactly this format: \"yyyy-MM-dd HH:mm:ss +0000\" (example: \"2026-06-12 23:31:14 +0000\")\n" +
                "5. Return ONLY a valid JSON array, no markdown, no explanation, no code blocks\n" +
                "\n" +
                "The JSON must follow this exact structure:\n" +
                "[\n" +
                "  {\n" +
                "    \"date\": \"YYYY-MM-DD\",\n" +
                "    \"topNews\": [\n" +
                "      {\n" +
                "        \"category\": \"CATEGORY_NAME\",\n" +
                "        \"articles\": [\n" +
                "          {\n" +
                "            \"title\": \"...\",\n" +
                "            \"description\": \"...\",\n" +
                "            \"url\": \"...\",\n" +
                "            \"publishedAt\": \"...\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]\n" +
                "\n" +
                "Important rules:\n" +
                "- Return ONLY the JSON, nothing else! - This is very important!\n" +
                "- Every day must appear exactly once\n" +
                "- Every category must appear exactly once per day\n" +
                "- Each category must have exactly 5 articles\n" +
                "- Do not invent or modify articles — only use what is provided\n" +
                "- Convert the publishedAt timestamp exactly as mentioned\n" +
                "- If and ONLY if the category field is null at no other condition, you can choose the best fitting category out of these 5: " +
                        "politics_government, sport, society, economy_business_finance, science_technology\n" +
                "- If fewer than 5 articles are available for a category, return only the articles that exist. Never pad or repeat entries to reach 5." +
                "\n" +
                "Here are the articles:\n" +
                "\n" +
                "Day 1:\n" + geminiServiceUtil.NewsListToJson(firstDay) +
                "\n" +
                "Day 2:\n" + geminiServiceUtil.NewsListToJson(secondDay) +
                "\n" +
                "Today:\n" + geminiServiceUtil.NewsListToJson(today)
                , null);
    }
    private GenerateContentResponse getGeminiNewsResponseOneDay(List<NewsResponseDto> newsOneDay) {
        return client.models.generateContent("gemini-3.5-flash",
                "You are a news ranking assistant. You will receive news articles from one single day, each containing multiple categories.\n" +
                        "\n" +
                        "Your task:\n" +
                        "1. For each category, select the 5 most important and relevant articles\n" +
                        "2. Base your ranking on: relevance, significance of the event, and informational value\n" +
                        "3. Remove duplicate articles (same story, different sources) — keep only the most informative version\n" +
                        "4. Convert all publishedAt timestamps to exactly this format: \"yyyy-MM-dd HH:mm:ss +0000\" (example: \"2026-06-12 23:31:14 +0000\")\n" +
                        "5. Return ONLY a valid JSON array, no markdown, no explanation, no code blocks\n" +
                        "\n" +
                        "The JSON must follow this exact structure:\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"date\": \"YYYY-MM-DD\",\n" +
                        "    \"topNews\": [\n" +
                        "      {\n" +
                        "        \"category\": \"CATEGORY_NAME\",\n" +
                        "        \"articles\": [\n" +
                        "          {\n" +
                        "            \"title\": \"...\",\n" +
                        "            \"description\": \"...\",\n" +
                        "            \"url\": \"...\",\n" +
                        "            \"publishedAt\": \"...\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "]\n" +
                        "\n" +
                        "Important rules:\n" +
                        "- Return ONLY the JSON, nothing else! - This is very important!\n" +
                        "- Every category must appear exactly once per day\n" +
                        "- Each category must have exactly 5 articles\n" +
                        "- Do not invent or modify articles — only use what is provided\n" +
                        "- Convert the publishedAt timestamp exactly as mentioned\n" +
                        "- If and ONLY if the category field is null at no other condition, you can choose the best fitting category out of these 5: " +
                        "politics_government, sport, society, economy_business_finance, science_technology\n" +
                        "- If fewer than 5 articles are available for a category, return only the articles that exist. Never pad or repeat entries to reach 5." +
                        "\n" +
                        "Here are the articles:\n" +
                        "\n" +
                        "Day:\n" + geminiServiceUtil.NewsListToJson(newsOneDay)
                , null);
    }

    private Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> parseAIResponse(GenerateContentResponse aiResponse) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[ XX][ XXX]");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> allNews = new HashMap<>();

        if (aiResponse == null) {
            return allNews;
        }

        String responseString = aiResponse.text().trim();

        if (responseString.startsWith("```")) {
            responseString = responseString.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
        }

        JSONArray root = null;
        try {
            root = new JSONArray(responseString);
        } catch (JSONException e) {
            e.printStackTrace(); // TODO: Error Handling -> wrong AI response pattern
        }

        if (root == null) {
            return allNews;
        }

        // for each existing date
        for (int i = 0; i < root.length(); i++) {
            JSONObject day = root.getJSONObject(i);
            String date = day.getString("date");

            JSONArray topNews = day.getJSONArray("topNews");

            Map<News_Categories, List<NewsResponseDto>> topNewsOneDayAllCategories = new HashMap<>();
            // for each existing category at one specific date
            for (int j = 0; j < topNews.length(); j++) {
                JSONObject categoryFilter = topNews.getJSONObject(j);

                String responseCategory = categoryFilter.getString("category");
                News_Categories category = currentsNewsUtil.getNewsCategoryFromResponseCategory(responseCategory);
                if (category == null) {
                    log.warn("Unknown category string from Gemini: '{}'", responseCategory);
                    continue;
                }

                JSONArray articles = categoryFilter.getJSONArray("articles");

                List<NewsResponseDto> topNewsOneDayOneCategory = new ArrayList<>();

                // for each existing article on one specific day and one category
                for (int k = 0; k < articles.length(); k++) {
                    JSONObject articlesJSONObject = articles.getJSONObject(k);

                    String title = articlesJSONObject.getString("title");
                    String description = articlesJSONObject.optString("description");
                    String url = articlesJSONObject.getString("url");

                    String publishedAt = articlesJSONObject.getString("publishedAt");
                    OffsetDateTime dateTime = OffsetDateTime.parse(publishedAt, dateTimeFormatter);

                    NewsResponseDto newsArticle = NewsResponseDto.builder()
                            .title(title)
                            .description(description)
                            .url(url)
                            .category(category)
                            .publishedAt(dateTime)
                            .build();

                    topNewsOneDayOneCategory.add(newsArticle);
                }
                topNewsOneDayAllCategories.put(category, topNewsOneDayOneCategory);
            }
            allNews.put(LocalDate.parse(date, dateFormatter), topNewsOneDayAllCategories);
        }
        return allNews;
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> getNewsOneDay(LocalDate date) {
        log.info("Requesting Gemini ranking for single day {}", date);
        List<NewsResponseDto> newsSingleDay = currentsNewsService.getNewsOneDay(date);
        log.info("Sending {} articles to Gemini", newsSingleDay.size());

        GenerateContentResponse aiResponse = getGeminiNewsResponseOneDay(newsSingleDay);
        log.info("Gemini response received, parsing...");

        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> result = parseAIResponse(aiResponse);
        log.info("Parsed {} days from Gemini response", result.size());
        return result;
    }

    public Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> getNewsAllDays(LocalDate date) {
        log.info("Requesting Gemini ranking for 3 days ending {}", date);
        List<NewsResponseDto> day1News = currentsNewsService.getNewsOneDay(date.minusDays(2));
        List<NewsResponseDto> day2News = currentsNewsService.getNewsOneDay(date.minusDays(1));
        List<NewsResponseDto> todayNews = currentsNewsService.getNewsOneDay(date);
        log.info("Sending {} articles to Gemini", day1News.size() + day2News.size() + todayNews.size());

        GenerateContentResponse aiResponse = getGeminiNewsResponseAllDays(day1News, day2News, todayNews);
        log.info("Gemini response received, parsing...");

        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> result = parseAIResponse(aiResponse);
        log.info("Parsed {} days from Gemini response", result.size());
        return result;
    }
}
