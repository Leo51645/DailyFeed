package com.github.leo51645.dailyfeed.domain.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class AssetResponseDto {
    private String name;
    private String symbol;
    private LocalDate date;
    private String exchangeTimezone;

    private BigDecimal currentPrice;
    private BigDecimal previousDayClosePrice;

    private BigDecimal changePercentClosedMarket;
    private BigDecimal changePercentIntraday;

    private boolean marketClosed;

}
