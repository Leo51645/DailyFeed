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
                endDate + "&domain_not=bild.de, merkur.de, express.de, tz.de, kleinezeitung.at, sueddeutsche.de&page_number=" + pageNumber;
    }
}
