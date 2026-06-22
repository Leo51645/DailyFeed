package com.github.leo51645.dailyfeed.service;

import com.github.leo51645.dailyfeed.domain.dto.AssetResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.Assets;
import com.github.leo51645.dailyfeed.util.YahooFinanceUtil;
import com.google.api.client.json.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class YahooFinanceService {

    private final YahooFinanceUtil yahooFinanceUtil;

    private final HttpClient client = HttpClient.newHttpClient();

    // sends HttpRequest to YahooFinanceAPI
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
            log.debug("Fetching data for asset: {}", asset.name());
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Yahoo Finance API returned HTTP {} for asset {}", response.statusCode(), asset.name());
                throw new RuntimeException("Yahoo Finance API error " + response.statusCode() + " for " + asset.getDisplayName());
            }

            return response;

        } catch (IOException e) {
            log.error("Network error fetching asset {}: {}", asset.name(), e.getMessage());
            throw new RuntimeException("Network error fetching " + asset.getDisplayName() + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("Request interrupted for asset {}", asset.name());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted for " + asset.getDisplayName(), e);
        }
    }

    // parses http response into Dto's
    private List<AssetResponseDto> parseHistoricalAssets(JSONObject root, Assets asset) {
        List<AssetResponseDto> assets = new ArrayList<>();

        JSONObject meta = yahooFinanceUtil.getMetaData(root);
        JSONArray timestamps = yahooFinanceUtil.getTimestamps(root);
        JSONArray close = yahooFinanceUtil.getClose(root);

        String name = asset.getDisplayName();
        String symbol = meta.getString("symbol");

        String exchangeTimezone = meta.getString("exchangeTimezoneName");

        // for each day in the response but starting on the 2. day, excluding the last (handled by parseLastTradingDayAsset)
        for (int i = 1; i < timestamps.length() - 1; i++) {
            LocalDate date = Instant.ofEpochSecond(timestamps.getLong(i))
                    .atZone(ZoneId.of(exchangeTimezone))
                    .toLocalDate();

            if (close.get(i) == JSONObject.NULL || close.get(i - 1) == JSONObject.NULL) {
                continue;
            }

            BigDecimal currentPrice = BigDecimal.valueOf(close.getDouble(i));
            BigDecimal previousClosePrice = BigDecimal.valueOf(close.getDouble(i - 1));

            AssetResponseDto dto = AssetResponseDto.builder()
                    .name(name)
                    .symbol(symbol)
                    .date(date)
                    .exchangeTimezone(exchangeTimezone)
                    .currentPrice(currentPrice)
                    .previousDayClosePrice(previousClosePrice)
                    .marketClosed(true)
                    .build();

            // calculate percentage difference in comparison to the day before
            BigDecimal changePercent = yahooFinanceUtil.percentageDifference(currentPrice, previousClosePrice);
            dto.setChangePercentClosedMarket(changePercent);

            assets.add(dto);
        }

        return assets;
    }
    private AssetResponseDto parseLastTradingDayAsset(JSONObject root, Assets asset) {

        JSONObject meta = yahooFinanceUtil.getMetaData(root);
        JSONArray timestamps = yahooFinanceUtil.getTimestamps(root);
        JSONArray close = yahooFinanceUtil.getClose(root);

        int lastIndex = timestamps.length() - 1;

        String name = asset.getDisplayName();
        String symbol = meta.getString("symbol");

        String exchangeTimezone = meta.getString("exchangeTimezoneName");

        LocalDate date = Instant.ofEpochSecond(timestamps.getLong(lastIndex))
                .atZone(ZoneId.of(exchangeTimezone))
                .toLocalDate();

        BigDecimal currentPrice  = BigDecimal.valueOf(meta.getDouble("regularMarketPrice"));

        BigDecimal previousClose = null;
        for (int i = 1; i <= 4; i++) {
            if (close.get(lastIndex - i) != JSONObject.NULL) {
                previousClose = BigDecimal.valueOf(close.getDouble(lastIndex - i));
                break;
            }
        }

        // market open?
        // if the current time lies between the start time and the close time the market is open
        long now = Instant.now().getEpochSecond();
        long start = meta.getJSONObject("currentTradingPeriod").getJSONObject("regular").getLong("start");
        long end = meta.getJSONObject("currentTradingPeriod").getJSONObject("regular").getLong("end");
        boolean marketOpen = now >= start && now <= end;

        BigDecimal changePercent = yahooFinanceUtil.percentageDifference(currentPrice, previousClose);

        AssetResponseDto dto = AssetResponseDto.builder()
                .name(name)
                .symbol(symbol)
                .date(date)
                .exchangeTimezone(exchangeTimezone)
                .currentPrice(currentPrice)
                .previousDayClosePrice(previousClose)
                .build();

        if (marketOpen) {
            dto.setChangePercentIntraday(changePercent);
            dto.setMarketClosed(false);
        } else {
            dto.setChangePercentClosedMarket(changePercent);
            dto.setMarketClosed(true);
        }

        return dto;
    }

    public Map<Assets, List<AssetResponseDto>> getAllAssets() {
        Map<Assets, JSONObject> roots = new HashMap<>();

        for (Assets asset : Assets.values()) {
            try {
                HttpResponse<String> httpResponse = getHttpAssetResponse(asset);
                JSONObject root = new JSONObject(httpResponse.body());
                roots.put(asset, root);
            } catch (RuntimeException e) {
                log.error("Failed to fetch data for asset {}: {}", asset.name(), e.getMessage());
            }
        }

        Map<Assets, List<AssetResponseDto>> historicalAssets = getHistoricalAssets(roots);
        Map<Assets, AssetResponseDto> lastTradingDayAssets = getLastTradingDayAssets(roots);

        // for each value list in the map a dto is getting added on top
        for (Map.Entry<Assets, List<AssetResponseDto>> entry : historicalAssets.entrySet()) {
            Assets asset = entry.getKey();
            List<AssetResponseDto> assets = entry.getValue();

            AssetResponseDto lastTradingDayAsset = lastTradingDayAssets.get(asset);

            if (lastTradingDayAsset != null) {
                assets.add(lastTradingDayAsset);
            }
        }
        log.info("Fetched market data for {}/{} assets", historicalAssets.size(), Assets.values().length);
        return historicalAssets;
    }

    public Map<Assets, List<AssetResponseDto>> getHistoricalAssets(Map<Assets, JSONObject> roots) {
        Map<Assets, List<AssetResponseDto>> assets = new HashMap<>();

        // for each value in the map the value is being parsed and then put into a results map
        for (Map.Entry<Assets, JSONObject> entry : roots.entrySet()) {
            Assets asset = entry.getKey();
            JSONObject root = entry.getValue();

            List<AssetResponseDto> responseList = parseHistoricalAssets(root, asset);
            assets.put(asset, responseList);
        }

        return assets;
    }

    public Map<Assets, AssetResponseDto> getLastTradingDayAssets(Map<Assets, JSONObject> roots) {
        Map<Assets, AssetResponseDto> assets = new HashMap<>();

        // for each value in the map the value is being parsed and then put into a results map
        for (Map.Entry<Assets, JSONObject> entry : roots.entrySet()) {
            Assets asset = entry.getKey();
            JSONObject root = entry.getValue();

            AssetResponseDto assetDto = parseLastTradingDayAsset(root, asset);
            assets.put(asset, assetDto);
        }

        return assets;
    }
}
