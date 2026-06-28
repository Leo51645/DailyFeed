package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
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
import java.util.concurrent.CompletableFuture;

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

            if (response.statusCode() == 400) {
                log.warn("Currents API returned HTTP 400 for category {} page {} — treating as last page", news_category, pageNumber);
                return null;
            }

            if (response.statusCode() != 200) {
                log.error("Currents API returned HTTP {} for category {} page {}", response.statusCode(), news_category, pageNumber);
                throw new RuntimeException("Currents API error " + response.statusCode() + " for category " + news_category);
            }

            log.debug("Currents API response received for category {} page {}", news_category, pageNumber);
            return response;
        } catch (IOException e) {
            log.error("Network error fetching news for category {}: {}", news_category, e.getMessage());
            throw new RuntimeException("Network error fetching news for " + news_category + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("Request interrupted for category {}", news_category);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted for category " + news_category, e);
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

    // get news in parallel sequence instead of one by one
    public List<NewsResponseDto> getNewsOneDay(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        log.info("Fetching news for date {}", date);
        List<NewsResponseDto> allNewsOneDay = new ArrayList<>();

        OffsetDateTime offsetDateTimeStartDate = OffsetDateTime.of(date, LocalTime.of(0, 0, 0), ZoneOffset.UTC);
        OffsetDateTime offsetDateTimeEndDate = OffsetDateTime.of(date, LocalTime.of(23, 59, 59), ZoneOffset.UTC);

        String startDate = offsetDateTimeStartDate.format(formatter);
        String endDate = offsetDateTimeEndDate.format(formatter);

        List<CompletableFuture<List<NewsResponseDto>>> futures = new ArrayList<>();

        for (News_Categories news_category : News_Categories.values()) {
            CompletableFuture<List<NewsResponseDto>> future = CompletableFuture.supplyAsync(() -> {
                boolean isNextPage = true;
                List<NewsResponseDto> newsListOneCategoryAllPages = new ArrayList<>();
                int i = 1;

                while (isNextPage) {
                    HttpResponse<String> httpResponse = getHttpNewsResponse(news_category, startDate, endDate, i);
                    i++;

                    if (httpResponse == null) break;

                    JSONObject root = new JSONObject(httpResponse.body());
                    boolean next_cursorIsNull = root.isNull("next_cursor");

                    if (next_cursorIsNull) isNextPage = false;

                    List<NewsResponseDto> newsListOneCategorySinglePage = parseNews(root);
                    newsListOneCategoryAllPages.addAll(newsListOneCategorySinglePage);
                }
                log.info("Fetched {} articles for category {}", newsListOneCategoryAllPages.size(), news_category);
                return newsListOneCategoryAllPages;
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<List<NewsResponseDto>> future : futures) {
            allNewsOneDay.addAll(future.join());
        }
        log.info("Total articles fetched for {}: {}", date, allNewsOneDay.size());
        return allNewsOneDay;
    }

    public HttpResponse<String> testApiKey(String currentsApiKey) throws Exception{
        URI testUrl = URI.create("https://api.currentsapi.services/v2/search?apiKey=" + currentsApiKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(testUrl)
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
