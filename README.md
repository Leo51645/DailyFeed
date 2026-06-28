# DailyFeed — Desktop News & Market Snapshot (Java / Spring Boot / Java Swing)

![Java 21](https://img.shields.io/badge/Java-21-blue?logo=java&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)  

DailyFeed is a Java desktop app that delivers the top 5 AI-ranked **German-language** news articles from 5 different categories as well as 11 specifically picked assets to represent the general market. With DailyFeed you
have an overview about what is happening in the world and how the stock market for example reacts to it.

Demo / Screenshots
- Screenshot: docs/screenshot.png
- Kurzes GIF: docs/demo.gif
(oder: Screenshot in der PR/Release anzeigen, wenn kein Live‑Link verfügbar ist)

## Techstack
Java 21 | Spring Boot | Java Swing  
  
**News:** Currents-News API -> [Currents API](https://currentsapi.services/en/)  
**Assets:** Yahoo-Finance API -> (unofficially)  
**AI Ranking:** Google AI Studio (Gemini-3.5-flash) -> [Gemini AI](https://aistudio.google.com/)

**Build:** Maven  
**Env:** java-dotenv (.env) -> [dotenv](https://github.com/cdimascio/dotenv-java) 


## Features
- **Top 5 News:** 5 most relevant News by Gemini AI from each of the 5 categories 
  - Categories: sport, politics, economy, technology & science, society
- **Live market data**: Big Tech(magnificent 7), S&P 500, DAX, Nikkei 225, Euro Stoxx 50, Gold, Silver, Bitcoin
- **3-day history:** browse today, yesterday and the day before
- **Local caching:** news results are saved as json files each containing one day and auto-cleaned after two days

## Getting Started

**Prerequirements:** Java 21 JDK, Maven  

1. Clone the repository:
```bash
git clone https://github.com/Leo51645/DailyFeed.git
cd DailyFeed
```

2. Build:
```bash
mvn clean package -DskipTests
```

3. Run:
```bash
java -jar target/DailyFeed-0.0.1-SNAPSHOT.jar
```

4. On the first launch you will be asked to put in your API Keys. [Currents API](https://currentsapi.services/en/) & [Google AI Studio](https://aistudio.google.com/api-keys). Sign in or create Account for free and copy
API-Keys

## Motivation
DailyFeed started as a school project in computer science, but I wanted to build something I can also benefit from in my daily life.
My interests in politics, finance and software engineering are all combined into this app.

## Technical Decisions
- **Swing instead of JavaFX or Web**: Swing is easier to implement and for my use case as a school project the decision with the least effort
- **JSON Cache over a database**: also less effort to set up and maintain -> easy inspect, delete and storage of JSON files
- **Parallel requests**: removes some of the long waiting time
- **Use of AI**: all of the service logic, util classes, enums and Dtos were written by me and discussed with the AI but only after I've had a clear approach and structure of what to build and how to build it. The whole
frontend code, log system and and exception handling on the other hand is strongly ai-assisted and generated because in my optinion all of that is just repetitive boilerplate code or irrelevant to backend development.

## Limitations

- **Frontend**: intentionally minimal and as easy as possible because the main focus on this project was on the backend
- **Loading times**: due to free ai services and APIs the product is relativeley slow when loading and rating news

## License

License
MIT — see [LICENSE](LICENSE)
