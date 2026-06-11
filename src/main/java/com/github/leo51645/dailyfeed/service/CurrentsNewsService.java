package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.response.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CurrentsNewsUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrentsNewsService {

    private final CurrentsNewsUtil currentsNewsUtil;

    @Value("${currents-news.apiKey}")
    private String apiKey;

    private final HttpClient client = HttpClient.newHttpClient();

    // before AI
    private HttpResponse<String> getHttpNewsResponse(News_Categories news_category, String startDate, String endDate, int pageNumber) {
        URI uri = URI.create(currentsNewsUtil.createCurrentsNewsRequestURI(news_category, apiKey, startDate, endDate, pageNumber));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.statusCode()); // Todo: Exception Handling
            }

            return response;
        } catch (IOException e) {
            throw new RuntimeException(e); //Todo: Exception Handling
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<NewsResponseDto> parseNews(JSONObject root) {
        JSONArray allNews = root.getJSONArray("news");

        List<NewsResponseDto> newsList = new ArrayList<>();
        for (int i = 0; i < allNews.length(); i++) {
            JSONObject singleNews = allNews.getJSONObject(i);

            String title = singleNews.getString("title");
            String description = singleNews.getString("description");
            String url = singleNews.getString("url");
            String responseCategory = singleNews.getJSONArray("category").getString(0);
            String publishedAt = singleNews.getString("published");

            News_Categories category = currentsNewsUtil.getNewsCategoryFromResponseCategory(responseCategory);

            DateTimeFormatter  formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
            OffsetDateTime dateTime = OffsetDateTime.parse(publishedAt, formatter);

            NewsResponseDto news = NewsResponseDto.builder()
                    .title(title)
                    .description(description)
                    .url(url)
                    .category(category)
                    .publishedAt(dateTime)
                    .build();

            newsList.add(news);
        }

        return newsList;
    }

    public List<NewsResponseDto> getNewsOneDay(LocalDate startDate, LocalDate endDate) {
        List<NewsResponseDto> allNewsOneDay = new ArrayList<>();

        for (News_Categories news_category : News_Categories.values()) {
            boolean isNextPage = true;
            List<NewsResponseDto> newsListOneCategoryAllPages = new ArrayList<>();
            int i = 1;

            while (isNextPage) {
                HttpResponse<String> httpResponse = getHttpNewsResponse(news_category, startDate.toString(), endDate.toString(), i);
                i++;

                if (httpResponse.statusCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + httpResponse.statusCode()); // Todo: Error Handling
                }

                JSONObject root = new JSONObject(httpResponse.body());
                boolean next_cursorIsNull = root.isNull("next_cursor");

                if (next_cursorIsNull) isNextPage = false;

                List<NewsResponseDto> newsListOneCategorySinglePage = parseNews(root);
                newsListOneCategoryAllPages.addAll(newsListOneCategorySinglePage);
            }
            allNewsOneDay.addAll(newsListOneCategoryAllPages);

        }
        return allNewsOneDay;
    }
}
