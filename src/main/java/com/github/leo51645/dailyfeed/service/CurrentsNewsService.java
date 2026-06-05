package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.util.CurrentsNewsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class CurrentsNewsService {

    private final CurrentsNewsUtil currentsNewsUtil;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> getHttpNewsResponse(News_Categories news_category, String apiKey, String startDate, String endDate, int pageNumber) {
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
}
