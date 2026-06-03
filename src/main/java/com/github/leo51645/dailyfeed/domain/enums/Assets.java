package com.github.leo51645.dailyfeed.domain.enums;

import lombok.Getter;

@Getter
public enum Assets {
    APPLE("AAPL", "Apple"),
    MICROSOFT("MSFT", "Microsoft"),
    GOOGLE("GOOGL", "Alphabet"),
    AMAZON("AMZN", "Amazon"),
    META("META", "Meta"),
    NVIDIA("NVDA", "Nvidia"),
    TESLA("TSLA", "Tesla"),
    SandP_500("^GSPC", "S&P500"),
    DAX("^GDAXI", "Dax"),
    NIKKEI_225("^N225", "Nikkei 225"),
    EURO_STOXX_50("^STOXX50E", "Euro Stoxx 50"),
    GOLD("GC=F", "Gold"),
    SILVER("SI=F", "Silver"),
    BITCOIN("BTC-USD", "Bitcoin");

    private final String symbol;
    private final String displayName;

    Assets(String symbol, String displayName) {
        this.symbol = symbol;
        this.displayName = displayName;
    }
}