package com.github.leo51645.dailyfeed.domain.dto;

import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class NewsResponseDto {
    private String title;
    private String description;

    private String url;
    private News_Categories category;

    private OffsetDateTime publishedAt;
}
