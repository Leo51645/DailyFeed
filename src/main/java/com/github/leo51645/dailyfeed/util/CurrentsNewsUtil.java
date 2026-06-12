package com.github.leo51645.dailyfeed.util;

import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import org.springframework.stereotype.Component;

@Component
public class CurrentsNewsUtil {

    public String createCurrentsNewsRequestURI(News_Categories news_category, String apiKey, String startDate, String endDate, int pageNumber) {
        return "https://api.currentsapi.services/v2/search?apiKey=" +
                apiKey + "&language=de&category=" +
                news_category.getCategoryName() + "&start_date=" +
                startDate + "&end_date=" +
                endDate + "&domain_not=&page_number=" + pageNumber;
    }

    public News_Categories getNewsCategoryFromResponseCategory(String responseCategory) {
        News_Categories category = null;

        if (responseCategory.equals(News_Categories.ECONOMY.getCategoryName())) {
            category = News_Categories.ECONOMY;
        } else if (responseCategory.equals(News_Categories.SOCIETY.getCategoryName())) {
            category = News_Categories.SOCIETY;
        } else if (responseCategory.equals(News_Categories.POLITICS.getCategoryName())) {
            category = News_Categories.POLITICS;
        } else if (responseCategory.equals(News_Categories.SPORTS.getCategoryName())) {
            category = News_Categories.SPORTS;
        } else if (responseCategory.equals(News_Categories.TECHNOLOGY_SCIENCE.getCategoryName())) {
            category = News_Categories.TECHNOLOGY_SCIENCE;
        }

        return category;
    }
}
