package com.github.leo51645.dailyfeed.domain.enums;

public enum News_Categories {
    POLITICS("politics_government", "Politk"),
    SPORTS("sports", "Sport"),
    SOCIETY("society", "Gesellschaft"),
    ECONOMY("economy_business_finance", "Wirtschaft"),
    TECHNOLOGY_SCIENCE("science_technology", "Wissenschaft & Technologie");

    private String categoryName;
    private String germanTranslation;

    News_Categories(String categoryName, String germanTranslation) {
        this.categoryName = categoryName;
        this.germanTranslation = germanTranslation;
    }
}
