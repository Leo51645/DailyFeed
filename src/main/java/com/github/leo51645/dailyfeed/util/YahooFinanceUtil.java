package com.github.leo51645.dailyfeed.util;

import com.github.leo51645.dailyfeed.domain.enums.Assets;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class YahooFinanceUtil {
    public String createYahooFinanceRequestURI(Assets asset) {
        return "https://query1.finance.yahoo.com/v8/finance/chart/"
                + asset.getSymbol()
                + "?range=6d&interval=1d&includePrePost=false&events=history&corsDomain=finance.yahoo.com";
    }

    public BigDecimal percentageDifference(BigDecimal currentPrice, BigDecimal previousClosePrice) {
        return currentPrice
                .subtract(previousClosePrice)
                .divide(previousClosePrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
