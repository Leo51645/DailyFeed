package com.github.leo51645.dailyfeed.ui;

import com.github.leo51645.dailyfeed.domain.dto.response.AssetResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.Assets;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * EAST region: full market overview as a table - one row per asset with name + ticker,
 * the price at the selected date and the percentage change for that day.
 */
public class MarketOverviewPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton refreshButton;

    public MarketOverviewPanel(Runnable onRefresh) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(270, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Marktübersicht");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));

        JLabel subtitle = new JLabel("Marktdaten immer von heute");
        subtitle.setFont(subtitle.getFont().deriveFont(10f));
        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(header);
        titlePanel.add(subtitle);

        refreshButton = new JButton("↻");
        refreshButton.setFont(refreshButton.getFont().deriveFont(16f));
        refreshButton.setToolTipText("Marktdaten aktualisieren");
        refreshButton.setMargin(new Insets(2, 6, 2, 6));
        refreshButton.addActionListener(e -> onRefresh.run());

        JPanel northPanel = new JPanel(new BorderLayout(6, 0));
        northPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 10, 0));
        northPanel.add(titlePanel, BorderLayout.CENTER);
        northPanel.add(refreshButton, BorderLayout.EAST);

        tableModel = new DefaultTableModel(new Object[]{"Asset", "Preis", "+/- %"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setCellRenderer(new AssetNameRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new PriceRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new ChangeRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /**
     * Fills the table with one row per known asset, picking the history entry that matches
     * the given date (falling back to the most recent entry if the exact date is missing).
     */
    public void setAssets(LocalDate date, Map<Assets, List<AssetResponseDto>> assetsByType) {
        tableModel.setRowCount(0);
        if (assetsByType == null) {
            return;
        }

        for (Assets asset : Assets.values()) {
            List<AssetResponseDto> history = assetsByType.get(asset);
            if (history == null || history.isEmpty()) {
                continue;
            }

            AssetResponseDto dto = findForDate(history, date);
            BigDecimal change = dto.isMarketClosed() ? dto.getChangePercentClosedMarket() : dto.getChangePercentIntraday();

            tableModel.addRow(new Object[]{
                    new Object[]{asset.getDisplayName(), asset.getSymbol(), dto.isMarketClosed()},
                    dto.getCurrentPrice(),
                    change
            });
        }
    }

    /** Shows/hides the loading state on the refresh button. */
    public void setRefreshing(boolean refreshing) {
        refreshButton.setEnabled(!refreshing);
        refreshButton.setText(refreshing ? "..." : "↻");
    }

    /** Clears the table, e.g. while new data is loading. */
    public void clear() {
        tableModel.setRowCount(0);
    }

    private AssetResponseDto findForDate(List<AssetResponseDto> history, LocalDate date) {
        for (AssetResponseDto dto : history) {
            if (date.equals(dto.getDate())) {
                return dto;
            }
        }
        return history.get(history.size() - 1);
    }

    private static class AssetNameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.LEFT);
            if (value instanceof Object[] data) {
                String name = (String) data[0];
                String symbol = (String) data[1];
                boolean closed = (boolean) data[2];
                String dot = closed
                        ? "<font color='#888888'>●</font>"
                        : "<font color='#00aa00'>●</font>";
                setText("<html><div>" + dot + " " + name
                        + "<br><font size='2' color='gray'>" + symbol + "</font></div></html>");
            }
            return this;
        }
    }

    private static class PriceRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (value instanceof BigDecimal price) {
                setText(String.format("%,.2f", price));
            } else {
                setText("–");
            }
            return this;
        }
    }

    private static class ChangeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (value instanceof BigDecimal change) {
                setText(String.format("%+.2f%%", change));
                setForeground(change.signum() < 0 ? new Color(190, 0, 0) : new Color(0, 130, 0));
            } else {
                setText("–");
                setForeground(Color.GRAY);
            }
            return this;
        }
    }
}
