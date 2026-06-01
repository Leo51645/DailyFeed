package com.github.leo51645.dailyfeed.domain.enums;

import lombok.Getter;

@Getter
public enum Assets {
    APPLE("AAPL"),
    MICROSOFT("MSFT"),
    GOOGLE("GOOGL"),
    AMAZON("AMZN"),
    META("META"),
    NVIDIA("NVDA"),
    TESLA("TSLA"),
    SandP_500("^GSPC"),
    DAX("^GDAXI"),
    NIKKEI_225("^N225"),
    EURO_STOXX_50("^STOXX50E"),
    GOLD("GC=F"),
    SILVER("SI=F"),
    BITCOIN("BTC-USD");

    private final String symbol;

    Assets(String symbol) {
        this.symbol = symbol;
    }
}