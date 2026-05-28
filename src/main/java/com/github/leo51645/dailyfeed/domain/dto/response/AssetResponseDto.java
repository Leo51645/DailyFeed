package com.github.leo51645.dailyfeed.domain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetResponseDto {
    private String name;
    private String symbol;
    private LocalDate date;

    private BigDecimal currentPrice;
    private BigDecimal previousClosePrice;

    private BigDecimal changePercentClosedMarket;
    private BigDecimal changePercentIntraday;

}
