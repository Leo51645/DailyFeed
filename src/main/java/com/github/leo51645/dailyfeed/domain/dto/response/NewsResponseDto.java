package com.github.leo51645.dailyfeed.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsResponseDto {
    private String title;
    private String description;

    private String url;
    private String category;

    private String publishedAt;
}
