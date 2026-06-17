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

    private static final int LIST_WIDTH = 420;

    private final Map<News_Categories, JToggleButton> categoryButtons = new LinkedHashMap<>();
    private final DefaultListModel<NewsResponseDto> listModel = new DefaultListModel<>();
    private final JList<NewsResponseDto> articleList = new JList<>(listModel);
    private final JPanel buttonPanel;

    private Map<News_Categories, List<NewsResponseDto>> newsByCategory = Map.of();
    private Consumer<NewsResponseDto> articleSelectedListener = article -> {};

    public NewsCategoryPanel() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(LIST_WIDTH, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ButtonGroup buttonGroup = new ButtonGroup();
        News_Categories[] categories = News_Categories.values();
        buttonPanel = new JPanel(new GridLayout(1, categories.length, 4, 0));

        for (News_Categories category : categories) {
            JToggleButton button = new JToggleButton(category.getGermanTranslation());
            button.setFont(button.getFont().deriveFont(12f));
            button.setMargin(new Insets(3, 6, 3, 6));
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

        JScrollPane scrollPane = new JScrollPane(articleList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    public JPanel getButtonPanel() {
        return buttonPanel;
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

    private static class ArticleListCellRenderer implements ListCellRenderer<NewsResponseDto> {

        @Override
        public Component getListCellRendererComponent(JList<? extends NewsResponseDto> list,
                                                       NewsResponseDto value, int index,
                                                       boolean isSelected, boolean cellHasFocus) {
            Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();
            Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();

            JLabel numLabel = new JLabel((index + 1) + ".");
            numLabel.setFont(numLabel.getFont().deriveFont(Font.BOLD, 13f));
            numLabel.setVerticalAlignment(SwingConstants.TOP);
            numLabel.setForeground(fg);
            numLabel.setBackground(bg);
            numLabel.setOpaque(true);

            JTextArea textArea = new JTextArea(value != null ? value.getTitle() : "");
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setOpaque(true);
            textArea.setBackground(bg);
            textArea.setForeground(fg);
            textArea.setFont(textArea.getFont().deriveFont(14f));

            // Give textArea its real width so getPreferredSize() returns the correct wrapped height
            int availWidth = list.getWidth() - 60;
            if (availWidth > 0) {
                textArea.setSize(new Dimension(availWidth, Integer.MAX_VALUE));
            }

            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            panel.setBackground(bg);
            panel.setOpaque(true);
            panel.add(numLabel, BorderLayout.WEST);
            panel.add(textArea, BorderLayout.CENTER);

            return panel;
        }
    }
}
