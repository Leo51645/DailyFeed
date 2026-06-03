# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=DailyFeedApplicationTests
```

## Architecture

DailyFeed is a Java 21 / Spring Boot 4 desktop app (Swing UI planned) that fetches stock/asset data from the Yahoo Finance API and news from the Currents API, then uses an AI to rank the top 5 news of the day by category.

**Key layers:**

- `domain/enums/Assets` — enum of all tracked assets (stocks, indices, commodities, crypto) with their Yahoo Finance ticker symbols
- `domain/dto/response/AssetResponseDto` — result object per asset per day: price, previous close, change percent (separate fields for intraday vs. closed market), and a `marketClosed` flag
- `util/YahooFinanceUtil` — builds the Yahoo Finance request URI (`range=6d&interval=1d`) and provides JSON path helpers (`getMetaData`, `getTimestamps`, `getClose`) and `percentageDifference`
- `service/YahooFinanceService` — sends HTTP requests, parses responses into DTOs. Two parsing paths: `parseHistoricalAssets` (days 1–N, uses close prices) and `parseLastTradingDayAsset` (uses `regularMarketPrice` from meta for live/current price, detects open market via `currentTradingPeriod`)

**Data flow:**
`getAllAssets()` fetches all assets once, then calls both `getHistoricalAssets()` and `getLastTradingDayAssets()` on the cached JSON, and merges the last-trading-day DTO onto the end of each asset's history list.

**Environment:** API keys are loaded via `java-dotenv` (`.env` file in project root). Spring Boot app context is used as a service locator in `main()` during development — this will be replaced by Swing UI wiring later.

## Planned features (not yet implemented)

- Currents API integration for news fetching (filtered per selected day)
- AI service to select top 5 news per day and categorize them
- Java Swing UI with day selector (last 5 trading days) showing asset movements + top news
