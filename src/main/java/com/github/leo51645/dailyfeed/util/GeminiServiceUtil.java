package com.github.leo51645.dailyfeed.util;
import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiServiceUtil {

    private final ObjectMapper objectMapper;

    public String NewsListToJson(List<NewsResponseDto> newsList) {
        StringBuilder stringBuilder = new StringBuilder();

        for (NewsResponseDto newsResponse : newsList) {
            String json = objectMapper.writeValueAsString(newsResponse);
            stringBuilder.append(json).append("\n");
        }
        return stringBuilder.toString();
    }
}
