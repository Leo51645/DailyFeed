package com.github.leo51645.dailyfeed.util;

import com.github.leo51645.dailyfeed.domain.enums.Assets;
import org.springframework.stereotype.Component;

@Component
public class YahooFinanceUtil {
    public String createYahooFinanceRequestURI(Assets asset) {
        return "https://query1.finance.yahoo.com/v8/finance/chart/"
                + asset.getSymbol()
                + "?range=5d&interval=1d&includePrePost=false&events=history&corsDomain=finance.yahoo.com";
    }
}
