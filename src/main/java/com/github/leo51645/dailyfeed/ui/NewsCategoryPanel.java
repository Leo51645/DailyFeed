package com.github.leo51645.dailyfeed.ui;

import com.github.leo51645.dailyfeed.domain.dto.response.NewsResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.News_Categories;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * WEST region: one clickable button per news category. Clicking a category loads its
 * top articles for the currently displayed day into the list below the buttons.
 */
public class NewsCategoryPanel extends JPanel {

    private static final int PANEL_WIDTH = 340;

    private final Map<News_Categories, JToggleButton> categoryButtons = new LinkedHashMap<>();
    private final DefaultListModel<NewsResponseDto> listModel = new DefaultListModel<>();
    private final JList<NewsResponseDto> articleList = new JList<>(listModel);

    private Map<News_Categories, List<NewsResponseDto>> newsByCategory = Map.of();
    private Consumer<NewsResponseDto> articleSelectedListener = article -> {};

    public NewsCategoryPanel() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(PANEL_WIDTH, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ButtonGroup buttonGroup = new ButtonGroup();
        News_Categories[] categories = News_Categories.values();
        // laid out horizontally (in a row) instead of stacked top-to-bottom
        JPanel buttonPanel = new JPanel(new GridLayout(1, categories.length, 4, 0));

        int buttonWidth = (PANEL_WIDTH - 20) / categories.length;
        for (News_Categories category : categories) {
            JToggleButton button = new JToggleButton(
                    "<html><div style='text-align:center; width:" + (buttonWidth - 10) + "px;'>"
                            + category.getGermanTranslation() + "</div></html>");
            button.setFont(button.getFont().deriveFont(11f));
            button.addActionListener(e -> selectCategory(category));

            buttonGroup.add(button);
            categoryButtons.put(category, button);
            buttonPanel.add(button);
        }

        articleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        articleList.setCellRenderer(new ArticleListCellRenderer());
        articleList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                NewsResponseDto selected = articleList.getSelectedValue();
                if (selected != null) {
                    articleSelectedListener.accept(selected);
                }
            }
        });

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(buttonPanel, BorderLayout.NORTH);
        northPanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(articleList), BorderLayout.CENTER);
    }

    public void setOnArticleSelected(Consumer<NewsResponseDto> listener) {
        this.articleSelectedListener = listener;
    }

    /** Replaces the displayed news and selects the first category by default. */
    public void setNews(Map<News_Categories, List<NewsResponseDto>> newsByCategory) {
        this.newsByCategory = newsByCategory != null ? newsByCategory : Map.of();
        News_Categories firstCategory = News_Categories.values()[0];
        categoryButtons.get(firstCategory).setSelected(true);
        selectCategory(firstCategory);
    }

    /** Clears all displayed news, e.g. while new data is loading. */
    public void clear() {
        this.newsByCategory = Map.of();
        listModel.clear();
    }

    private void selectCategory(News_Categories category) {
        listModel.clear();
        for (NewsResponseDto article : newsByCategory.getOrDefault(category, List.of())) {
            listModel.addElement(article);
        }
    }

    private static class ArticleListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                    BorderFactory.createEmptyBorder(12, 10, 12, 10)));
            setFont(getFont().deriveFont(14f));
            if (value instanceof NewsResponseDto article) {
                setText("<html><body style='width: " + (PANEL_WIDTH - 50) + "px'>"
                        + escapeHtml(article.getTitle()) + "</body></html>");
            }
            return this;
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
