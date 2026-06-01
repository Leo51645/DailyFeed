package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.response.AssetResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.Assets;
import com.github.leo51645.dailyfeed.util.YahooFinanceUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YahooFinanceService {

    public final YahooFinanceUtil yahooFinanceUtil;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> getHttpAssetResponse(Assets asset) {
        URI uri = URI.create(yahooFinanceUtil.createYahooFinanceRequestURI(asset));



        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(5))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/120.0.0.0 Safari/537.36") // signals to the server that you're a browser
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.statusCode()); // Todo: Exception Handling
            }

            return response;

        } catch (IOException e) {
            throw new RuntimeException(e); // Todo: Error response schreiben etc.
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<AssetResponseDto> parseResponse(String responseBody) {
        JSONObject root = new JSONObject(responseBody);
        List<AssetResponseDto> assets = new ArrayList<>();

        JSONObject meta = root
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONObject("meta");

        JSONArray timestamps = root
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONArray("timestamp");

        JSONArray close = root
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONObject("indicators")
                .getJSONArray("quote")
                .getJSONObject(0)
                .getJSONArray("close");

        String name = meta.getString("longName");
        String symbol = meta.getString("symbol");

        String exchangeTimezone = meta.getString("exchangeTimezoneName");

        // market open?
        // if the current time lies between the start time and the close time the market is open
        long now = Instant.now().getEpochSecond();
        long start = meta.getJSONObject("currentTradingPeriod").getJSONObject("regular").getLong("start");
        long end = meta.getJSONObject("currentTradingPeriod").getJSONObject("regular").getLong("end");
        boolean marketOpen = now >= start && now <= end;

        // for each day in the response but starting on the 2. day
        for (int i = 1; i < timestamps.length(); i++) {
            LocalDate date = Instant.ofEpochSecond(timestamps.getLong(i))
                    .atZone(ZoneId.of(exchangeTimezone))
                    .toLocalDate();

            BigDecimal currentPrice;
            boolean isLastEntry = i == timestamps.length() - 1;

            // if day is today take the price from the market that is still open
            if (isLastEntry && marketOpen) {
                currentPrice = BigDecimal.valueOf(meta.getDouble("regularMarketPrice"));
            } else {
                currentPrice = BigDecimal.valueOf(close.getDouble(i));
            }

            BigDecimal previousClosePrice = BigDecimal.valueOf(close.getDouble(i - 1));

            AssetResponseDto dto = AssetResponseDto.builder()
                    .name(name)
                    .symbol(symbol)
                    .date(date)
                    .currentPrice(currentPrice)
                    .previousDayClosePrice(previousClosePrice)
                    .build();

            // calculate percentage difference in comparison to the day before
            BigDecimal changePercent = currentPrice
                    .subtract(previousClosePrice)
                    .divide(previousClosePrice, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (isLastEntry && marketOpen) {
                dto.setChangePercentIntraday(changePercent);
            } else {
                dto.setChangePercentClosedMarket(changePercent);
            }

            assets.add(dto);
        }

        return assets;
    }
}
