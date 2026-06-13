package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.response.NewsResponseDto;
import com.github.leo51645.dailyfeed.util.GeminiServiceUtil;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client client = new Client();
    private final GeminiServiceUtil geminiServiceUtil;

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
}
