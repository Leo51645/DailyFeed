package com.github.leo51645.dailyfeed.util;

import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import org.springframework.stereotype.Component;

@Component
public class CurrentsNewsUtil {

    public String createCurrentsNewsRequestURI(News_Categories news_category, String apiKey, String startDate, String endDate, int pageNumber) {
        return "https://api.currentsapi.services/v2/search?apiKey=" +
                apiKey + "&language=de&category=" +
                news_category.getCategoryName() + "&start_date=2026-06-13T00:00:00Z&end_date=2026-06-13T23:59:59Z" +
                "&domain_not=noz.de,bild.de,kleinezeitung.de,merkur.de,sueddeutsche.de,tt.com,focus.de,taz.de,cicero.de," +
                "nachdenkseiten.de,rollingstone.de,golem.de,presseportal.de,woz.ch,blick.ch,hna.de,watson.de,manager-magazin.de," +
                "t3n.de,heise.de,spektrum.de,hardwareluxx.de,blog.gwup.net" +
                "&page_number=" + pageNumber;
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
