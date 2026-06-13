package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.response.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CurrentsNewsUtil;
import com.github.leo51645.dailyfeed.util.GeminiServiceUtil;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.swing.text.DateFormatter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client client = new Client();
    private final GeminiServiceUtil geminiServiceUtil;
    private final CurrentsNewsUtil currentsNewsUtil;

    // create AI prompt out off all news, send it and get response of the gemini AI
    private GenerateContentResponse getGeminiNewsResponse(List<NewsResponseDto> firstDay, List<NewsResponseDto> secondDay, List<NewsResponseDto> today) {
        return client.models.generateContent("gemini-2.5-flash",
                "You are a news ranking assistant. You will receive news articles from three different days, each containing multiple categories.\n" +
                "\n" +
                "Your task:\n" +
                "1. For each day and each category, select the 5 most important and relevant articles\n" +
                "2. Base your ranking on: relevance, significance of the event, and informational value\n" +
                "3. Remove duplicate articles (same story, different sources) — keep only the most informative version\n" +
                "4. Return ONLY a valid JSON array, no markdown, no explanation, no code blocks\n" +
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
                "- Return ONLY the JSON, nothing else\n" +
                "- Every day must appear exactly once\n" +
                "- Every category must appear exactly once per day\n" +
                "- Each category must have exactly 5 articles\n" +
                "- Do not invent or modify articles — only use what is provided\n" +
                "- Preserve the original publishedAt timestamp exactly as provided\n" +
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

    private Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> parseAIResponse(GenerateContentResponse aiResponse) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String responseString = aiResponse.text();
        JSONArray root = new JSONArray(responseString);

        Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>> allNews = new HashMap<>();

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
            allNews.put(OffsetDateTime.parse(date, dateFormatter).toLocalDate(), topNewsOneDayAllCategories);
        }
        return allNews;
    }
}
