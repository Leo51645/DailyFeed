package com.github.leo51645.dailyfeed.ui;

import com.github.leo51645.dailyfeed.domain.dto.AssetResponseDto;
import com.github.leo51645.dailyfeed.domain.dto.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.Assets;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;
import com.github.leo51645.dailyfeed.service.NewsFetchCoordinator;
import com.github.leo51645.dailyfeed.service.YahooFinanceService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main application window. Wires together the four regions described in CLAUDE.md:
 * NORTH = date selector + load button, WEST = news categories, CENTER = article detail,
 * EAST = market overview table.
 */
@Slf4j
public class MainFrame extends JFrame {

    private static final DateTimeFormatter LOADED_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final YahooFinanceService yahooFinanceService;
    private final NewsFetchCoordinator newsFetchCoordinator;

    private final TopBarPanel topBarPanel;
    private final NewsCategoryPanel newsCategoryPanel;
    private final ArticleDetailPanel articleDetailPanel;
    private final MarketOverviewPanel marketOverviewPanel;
    private final JLabel loadedDateLabel = new JLabel("Noch keine Daten geladen.", SwingConstants.CENTER);

    private ConcurrentHashMap<LocalDate, Map<News_Categories, List<NewsResponseDto>>> allNewsMap = new ConcurrentHashMap<>();

    public MainFrame(YahooFinanceService yahooFinanceService, NewsFetchCoordinator newsFetchCoordinator) {
        super("DailyFeed");
        this.yahooFinanceService = yahooFinanceService;
        this.newsFetchCoordinator = newsFetchCoordinator;

        Image icon = loadIconImage();
        if (icon != null) {
            setIconImage(icon);
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);

        topBarPanel = new TopBarPanel(this::loadData);
        newsCategoryPanel = new NewsCategoryPanel();
        articleDetailPanel = new ArticleDetailPanel();
        marketOverviewPanel = new MarketOverviewPanel(this::refreshMarket);

        newsCategoryPanel.setOnArticleSelected(articleDetailPanel::showArticle);

        loadedDateLabel.setForeground(Color.GRAY);
        loadedDateLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(6, 0, 6, 0)));

        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        buttonWrapper.add(newsCategoryPanel.getButtonPanel(), BorderLayout.CENTER);

        JPanel newsAndDetail = new JPanel(new BorderLayout());
        newsAndDetail.add(newsCategoryPanel, BorderLayout.WEST);
        newsAndDetail.add(articleDetailPanel, BorderLayout.CENTER);

        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.add(buttonWrapper, BorderLayout.NORTH);
        centerContent.add(newsAndDetail, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topBarPanel, BorderLayout.NORTH);
        add(centerContent, BorderLayout.CENTER);
        add(marketOverviewPanel, BorderLayout.EAST);
        add(loadedDateLabel, BorderLayout.SOUTH);
    }

    public void loadInitialData(SplashScreen splash) {
        new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() {
                LocalDate today = LocalDate.now();
                Map<News_Categories, List<NewsResponseDto>> news = fetchNews(today);
                Map<Assets, List<AssetResponseDto>> assets = yahooFinanceService.getAllAssets();
                return new LoadResult(news, assets);
            }

            @Override
            protected void done() {
                splash.hideSplash();
                setVisible(true);
                try {
                    LoadResult result = get();
                    LocalDate today = LocalDate.now();
                    newsCategoryPanel.setNews(result.news());
                    marketOverviewPanel.setAssets(today, result.assets());
                    loadedDateLabel.setForeground(Color.GRAY);
                    loadedDateLabel.setText("Angezeigte Daten vom: " + today.format(LOADED_DATE_FORMATTER));
                } catch (Exception e) {
                    log.error("Failed to load initial data", e);
                }
            }
        }.execute();
    }

    private void loadData(LocalDate date) {
        topBarPanel.setLoading(true);
        articleDetailPanel.clear();

        new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() {
                Map<News_Categories, List<NewsResponseDto>> news = fetchNews(date);
                Map<Assets, List<AssetResponseDto>> assets = yahooFinanceService.getAllAssets();
                return new LoadResult(news, assets);
            }

            @Override
            protected void done() {
                topBarPanel.setLoading(false);
                try {
                    LoadResult result = get();
                    newsCategoryPanel.setNews(result.news());
                    marketOverviewPanel.setAssets(LocalDate.now(), result.assets());
                    loadedDateLabel.setForeground(Color.GRAY);
                    loadedDateLabel.setText("Angezeigte Daten vom: " + date.format(LOADED_DATE_FORMATTER));
                } catch (Exception e) {
                    log.error("Failed to load data for {}", date, e);
                    topBarPanel.setError("Fehler beim Laden der Daten.");
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Daten konnten nicht geladen werden:\n" + e.getCause(),
                            "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void refreshMarket() {
        marketOverviewPanel.setRefreshing(true);
        new SwingWorker<Map<Assets, List<AssetResponseDto>>, Void>() {
            @Override
            protected Map<Assets, List<AssetResponseDto>> doInBackground() {
                return yahooFinanceService.getAllAssets();
            }

            @Override
            protected void done() {
                marketOverviewPanel.setRefreshing(false);
                try {
                    marketOverviewPanel.setAssets(LocalDate.now(), get());
                } catch (Exception e) {
                    log.error("Failed to refresh market data", e);
                }
            }
        }.execute();
    }

    private Map<News_Categories, List<NewsResponseDto>> fetchNews(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today) || !allNewsMap.containsKey(date)) {
            allNewsMap.putAll(newsFetchCoordinator.combineCachedMissingNews(today));
        }
        return allNewsMap.getOrDefault(date, Map.of());
    }

    private Image loadIconImage() {
        URL url = getClass().getResource("/images/dailyFeedLogo.png");
        return url != null ? new ImageIcon(url).getImage() : null;
    }

    private record LoadResult(Map<News_Categories, List<NewsResponseDto>> news,
                               Map<Assets, List<AssetResponseDto>> assets) {
    }
}
