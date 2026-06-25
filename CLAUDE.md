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

# Interaction Rules
- Never provide ready-to-use code solutions, code examples to help get the solution is fine
- When I share an error: explain what the error means, not how to fix it
- Ask me what I think the cause is before giving any hints
- If I'm completely stuck, give only a directional hint — not the solution
- Review my code for quality/security after I've written it myself
- Boilerplate and config (Docker, YAML, dependencies) can be provided directly
- All of this only is active while backend development. The creation of the frontend especially is fully allowed by AI because my focus is lying on learning backend and not frontend.

## Architecture

DailyFeed is a Java 21 / Spring Boot desktop app (Swing UI) that fetches stock/asset data from the Yahoo Finance API and news from the Currents API, then uses Google Gemini to rank the top 5 news per category per day.

**Key layers:**

- `domain/enums/Assets` — enum of all tracked assets with Yahoo Finance ticker symbols
- `domain/enums/News_Categories` — enum of all news categories with Currents API name and German translation
- `domain/dto/AssetResponseDto` — price, previous close, change percent (separate fields for intraday vs. closed market), marketClosed flag
- `domain/dto/NewsResponseDto` — title, description, url, category, publishedAt
- `util/YahooFinanceUtil` — builds Yahoo Finance URI, JSON path helpers, percentageDifference
- `util/CurrentsNewsUtil` — builds Currents API URI, maps response category string to News_Categories enum
- `util/GeminiServiceUtil` — serializes List<NewsResponseDto> to JSON string for Gemini prompt
- `util/CacheUtil` — builds file path for cache files (cache/day_YYYY-MM-DD.json)

**Services:**

- `YahooFinanceService` — fetches all assets, two parsing paths: parseHistoricalAssets (days 1–N-1) and parseLastTradingDayAsset (uses regularMarketPrice, detects open market via currentTradingPeriod unix timestamps)
- `CurrentsNewsService` — fetches news per day per category in parallel via CompletableFuture, paginates via next_cursor, returns List<NewsResponseDto>
- `GeminiService` — three prompt variants (1/2/3 days), parses AI JSON response to Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>>, also parses to Map<String, JSONObject> for caching
- `CacheService` — saves/loads per-day JSON files, cleans up files older than 2 days on startup, tracks last fetch timestamp
- `NewsFetchCoordinator` — decides what to fetch based on cache state: both days missing → getNewsTwoDays, only today missing → getNewsTwoDays, today cached but >30min old → getNewsOneDay. Triggers today-2 in background after 15s delay.

**Data flow:**
`MainFrame` triggers `NewsFetchCoordinator.combineCachedMissingNews()` → loads cache → determines missing days → calls GeminiService → saves new cache → merges and returns Map<LocalDate, Map<News_Categories, List<NewsResponseDto>>>. Market data via `YahooFinanceService.getAllAssets()` runs in parallel in the same SwingWorker.

**UI (fully AI-generated):**

- `MainFrame` — BorderLayout root, wires all panels, SwingWorker for background loading
- `TopBarPanel` — NORTH: logo, date selector (today/yesterday/day before), load button
- `NewsCategoryPanel` — WEST: category toggle buttons + scrollable article list
- `ArticleDetailPanel` — CENTER: title, meta, description, open-in-browser button
- `MarketOverviewPanel` — EAST: JTable with asset name, price, % change (green/red)
- `SplashScreen` — shown during initial load
- `ApiKeySetupDialog` — first-run and edit dialog for Gemini + Currents API keys

**Environment:**
API keys loaded via `java-dotenv` from `.env` in project root. Keys: `GEMINI_API_KEY`, `CURRENTS_API_KEY`. Spring Boot headless=false for Swing.

## Status

Project is complete. All services implemented and UI finished.
