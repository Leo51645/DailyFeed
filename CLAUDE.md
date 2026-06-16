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

# Developer Profile
15-year-old self-taught backend developer. Learning is the priority over speed.

# Interaction Rules
- Never provide ready-to-use code solutions, code examples to help get the solution is fine
- When I share an error: explain what the error means, not how to fix it
- Ask me what I think the cause is before giving any hints
- If I'm completely stuck, give only a directional hint — not the solution
- Review my code for quality/security after I've written it myself
- Boilerplate and config (Docker, YAML, dependencies) can be provided directly
- All of this only is active while backend development. The creation of the frontend especially is fully allowed by AI because my focus is lying on learning backend and not frontend.

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

## Next task: Currents API integration

Build a `CurrentsApiService` that fetches news for a given date, analogous to `YahooFinanceService`.

**Currents API details:**
- Base URL: `https://api.currentsapi.services/v1/`
- Relevant endpoint: `GET /v1/search`
- API key is stored in `.env` as `CURRENTS_API_KEY`, loaded via java-dotenv
- Key query parameters: `start_date`, `end_date` (ISO 8601: `2026-06-03T00:00:00+00:00`), `language=en`, `category`
- Categories to fetch: `politics_government`, `economy_business_finance`, `science_technology`, `general`, `arts_culture_entertainment` (maps to: Politik, Wirtschaft, Technologie, Weltgeschehen, Gesellschaft)
- **All valid category values:** `general`, `society`, `science_technology`, `politics_government`, `economy_business_finance`, `arts_culture_entertainment`, `lifestyle_leisure`, `human_interest`, `sport`, `crime_law_justice`, `education`, `environment`, `labour`, `health`, `automotive`, `real_estate`
- Response structure: `{ "news": [ { "title", "description", "url", "author", "published", "category": [...] } ] }`
- The API filters by `start_date` and `end_date` precisely

**What to build:**
1. `domain/dto/response/NewsArticleDto` — fields: `title`, `description`, `url`, `publishedAt` (LocalDate), `category`
2. `util/CurrentsApiUtil` — builds the request URI (similar pattern to `YahooFinanceUtil`)
3. `service/CurrentsApiService` — fetches news for a given `LocalDate`, returns `List<NewsArticleDto>` filtered to that day

**After that:** AI service (Claude API) that takes the `List<NewsArticleDto>` for a day and returns the top 5 articles with category labels.

**Then:** Swing UI with day selector showing asset movements + top 5 news per day.

**Deadline:** 2026-06-22

## Frontend Design (Swing UI)

Layout, top to bottom / left to right:

- **Top bar (NORTH):** Date selector limited to the last 2 days plus today, plus a "Load" button to fetch/display data for the selected date.
- **Left column (WEST):** The 5 news categories (Politik, Wirtschaft, Technologie, Weltgeschehen, Gesellschaft) shown as clickable buttons/tabs. Clicking a category loads and displays its top 5 article headlines in a list below the buttons.
- **Center column (CENTER):** Article detail panel. Stays empty/inactive until a headline from the left column is clicked; once clicked, it shows that article's headline, published time, source, and description.
- **Right column (EAST):** Full market overview as a table — one row per asset, columns: name (with ticker symbol underneath), current price at that point in time, and percentage change.
