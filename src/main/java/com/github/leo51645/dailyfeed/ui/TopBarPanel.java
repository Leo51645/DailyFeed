package com.github.leo51645.dailyfeed.ui;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * NORTH region: logo/title, a date selector limited to today and the two previous days,
 * a "Laden" button to trigger fetching/display of data for the selected date, and a status label.
 */
public class TopBarPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final JComboBox<LocalDate> dateComboBox;
    private final JButton loadButton;
    private final JLabel statusLabel;
    private boolean isLoading = false;

    public TopBarPanel(Consumer<LocalDate> onLoad, Runnable onEditApiKeys) {
        super(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel(loadScaledIcon("/images/dailyFeedLogo.png", 28, 28));
        leftPanel.add(logoLabel);

        JLabel titleLabel = new JLabel("DailyFeed");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        leftPanel.add(titleLabel);

        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(new JLabel("Datum:"));

        LocalDate today = LocalDate.now();
        dateComboBox = new JComboBox<>(new LocalDate[]{today, today.minusDays(1), today.minusDays(2)});
        dateComboBox.setRenderer(new DateListCellRenderer());
        dateComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (isLoading) SwingUtilities.invokeLater(dateComboBox::hidePopup);
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });
        leftPanel.add(dateComboBox);

        loadButton = new JButton("Laden");
        loadButton.addActionListener(e -> {
            LocalDate selected = (LocalDate) dateComboBox.getSelectedItem();
            if (selected != null) {
                onLoad.accept(selected);
            }
        });
        leftPanel.add(loadButton);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);
        leftPanel.add(statusLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JButton editKeysButton = new JButton("API Keys bearbeiten");
        editKeysButton.addActionListener(e -> onEditApiKeys.run());
        rightPanel.add(editKeysButton);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    /** Disables interaction and shows a loading hint while data is being fetched. */
    public void setLoading(boolean loading) {
        this.isLoading = loading;
        loadButton.setEnabled(!loading);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setText(loading ? "Lädt Daten..." : " ");
    }

    public void setError(String message) {
        statusLabel.setForeground(new Color(200, 0, 0));
        statusLabel.setText(message);
    }

    private static ImageIcon loadScaledIcon(String resourcePath, int width, int height) {
        URL url = TopBarPanel.class.getResource(resourcePath);
        if (url == null) {
            return new ImageIcon();
        }
        Image image = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private static class DateListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof LocalDate date) {
                LocalDate today = LocalDate.now();
                String label;
                if (date.equals(today)) {
                    label = "Heute (" + date.format(DATE_FORMATTER) + ")";
                } else if (date.equals(today.minusDays(1))) {
                    label = "Gestern (" + date.format(DATE_FORMATTER) + ")";
                } else {
                    label = "Vorgestern (" + date.format(DATE_FORMATTER) + ")";
                }
                setText(label);
            }
            return this;
        }
    }
}
