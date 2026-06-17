package com.github.leo51645.dailyfeed.ui;

import com.github.leo51645.dailyfeed.domain.dto.response.NewsResponseDto;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.time.format.DateTimeFormatter;

/**
 * CENTER region: stays empty/inactive until a headline is selected in {@link NewsCategoryPanel},
 * then shows that article's title, published time, source and description.
 */
public class ArticleDetailPanel extends JPanel {

    private static final String EMPTY_CARD = "empty";
    private static final String DETAIL_CARD = "detail";
    private static final DateTimeFormatter PUBLISHED_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final CardLayout cardLayout = new CardLayout();
    private final JLabel titleLabel = new JLabel();
    private final JLabel metaLabel = new JLabel();
    private final JTextArea descriptionArea = new JTextArea();
    private final JButton openLinkButton = new JButton("Artikel öffnen");

    private String currentUrl;

    public ArticleDetailPanel() {
        super();
        setLayout(cardLayout);

        add(buildEmptyCard(), EMPTY_CARD);
        add(buildDetailCard(), DETAIL_CARD);

        cardLayout.show(this, EMPTY_CARD);
    }

    private JComponent buildEmptyCard() {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        JLabel placeholder = new JLabel("Wähle eine Nachricht aus der Liste aus.");
        placeholder.setForeground(Color.GRAY);
        emptyPanel.add(placeholder);
        return emptyPanel;
    }

    private JComponent buildDetailCard() {
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(16, 30, 16, 16));

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        metaLabel.setForeground(Color.GRAY);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(metaLabel);
        headerPanel.add(Box.createVerticalStrut(14));

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(16f));

        openLinkButton.addActionListener(e -> openCurrentUrl());

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        footerPanel.add(openLinkButton);

        detailPanel.add(headerPanel, BorderLayout.NORTH);
        detailPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        detailPanel.add(footerPanel, BorderLayout.SOUTH);
        return detailPanel;
    }

    public void showArticle(NewsResponseDto article) {
        titleLabel.setText("<html><body style='width: 380px'>" + escapeHtml(article.getTitle()) + "</body></html>");

        String published = article.getPublishedAt() != null
                ? article.getPublishedAt().format(PUBLISHED_FORMATTER)
                : "unbekannt";
        metaLabel.setText("Quelle: " + extractSource(article.getUrl()) + "   |   Veröffentlicht: " + published);

        descriptionArea.setText(article.getDescription());
        descriptionArea.setCaretPosition(0);

        currentUrl = article.getUrl();
        cardLayout.show(this, DETAIL_CARD);
    }

    /** Resets to the empty placeholder, e.g. when new data is being loaded. */
    public void clear() {
        currentUrl = null;
        cardLayout.show(this, EMPTY_CARD);
    }

    private void openCurrentUrl() {
        if (currentUrl == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(currentUrl));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Link konnte nicht geöffnet werden.", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String extractSource(String url) {
        if (url == null) {
            return "unbekannt";
        }
        try {
            String host = URI.create(url).getHost();
            if (host == null) {
                return url;
            }
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return url;
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
