package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.response.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CurrentsNewsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
                log.error("Currents API error {} for category {} on page {} with http url {}", response.statusCode(), news_category, pageNumber, uri);
                System.out.println(response.body());
                throw new RuntimeException("Failed : HTTP error code : " + response.statusCode()); // Todo: Exception Handling
            }

            log.debug("Currents API response received for category {} page {}", news_category, pageNumber);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e); //Todo: Exception Handling
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // parses response from currents api to NewsResponseDto's
    private List<NewsResponseDto> parseNews(JSONObject root) {
        JSONArray allNews = root.getJSONArray("news");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");

        List<NewsResponseDto> newsList = new ArrayList<>();

        // for each news article from one category and one day create an object of it and save it in a list
        for (int i = 0; i < allNews.length(); i++) {
            JSONObject singleNews = allNews.getJSONObject(i);

            String title = singleNews.getString("title");
            // if article is a spiegel+ article skip this one
            if (title.contains("(S+)")) continue;

            String description = singleNews.getString("description");
            String url = singleNews.getString("url");
            JSONArray responseCategories = singleNews.getJSONArray("category");

            if (responseCategories.isEmpty()) continue;

            String responseCategory= responseCategories.getString(0);
            String publishedAt = singleNews.getString("published");
            News_Categories category = currentsNewsUtil.getNewsCategoryFromResponseCategory(responseCategory);
            OffsetDateTime dateTime = OffsetDateTime.parse(publishedAt, formatter);

            NewsResponseDto newsArticle = NewsResponseDto.builder()
                    .title(title)
                    .description(description)
                    .url(url)
                    .category(category)
                    .publishedAt(dateTime)
                    .build();

            newsList.add(newsArticle);
        }

        return newsList;
    }

    public List<NewsResponseDto> getNewsOneDay(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        log.info("Fetching news for date {}", date);
        List<NewsResponseDto> allNewsOneDay = new ArrayList<>();

        OffsetDateTime offsetDateTimeStartDate = OffsetDateTime.of(date, LocalTime.of(0, 0, 0), ZoneOffset.UTC);
        OffsetDateTime endDate = OffsetDateTime.of(date, LocalTime.of(23, 59, 59), ZoneOffset.UTC);

        // for each existing category get all articles of a specific day, save them in a list and put these lists together so there is one list with all categories for each day
        for (News_Categories news_category : News_Categories.values()) {
            boolean isNextPage = true;
            List<NewsResponseDto> newsListOneCategoryAllPages = new ArrayList<>();
            int i = 1;

            String startDate = offsetDateTimeStartDate.format(formatter);

            while (isNextPage) {
                HttpResponse<String> httpResponse = getHttpNewsResponse(news_category, startDate.toString(), endDate.toString(), i);
                i++;

                JSONObject root = new JSONObject(httpResponse.body());
                boolean next_cursorIsNull = root.isNull("next_cursor");

                if (next_cursorIsNull) isNextPage = false;

                List<NewsResponseDto> newsListOneCategorySinglePage = parseNews(root);
                newsListOneCategoryAllPages.addAll(newsListOneCategorySinglePage);
            }
            log.info("Fetched {} articles for category {}", newsListOneCategoryAllPages.size(), news_category);
            allNewsOneDay.addAll(newsListOneCategoryAllPages);
        }
        log.info("Total articles fetched for {}: {}", date, allNewsOneDay.size());
        return allNewsOneDay;
    }
}
