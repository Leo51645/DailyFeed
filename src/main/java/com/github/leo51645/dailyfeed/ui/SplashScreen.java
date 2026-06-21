package com.github.leo51645.dailyfeed.ui;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

    private final JProgressBar progressBar;

    public SplashScreen() {
        setSize(400, 220);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(30, 30, 30));
        content.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 1));

        JLabel title = new JLabel("DailyFeed", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 10, 0));

        JLabel subtitle = new JLabel("Daten werden geladen...", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(160, 160, 160));
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        progressBar.setBackground(new Color(50, 50, 50));
        progressBar.setForeground(new Color(100, 160, 255));

        progressBar.setPreferredSize(new Dimension(320, 18));

        JPanel progressPanel = new JPanel();
        progressPanel.setBackground(new Color(30, 30, 30));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        progressPanel.add(progressBar);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(new Color(30, 30, 30));
        textPanel.add(title, BorderLayout.NORTH);
        textPanel.add(subtitle, BorderLayout.CENTER);

        content.add(textPanel, BorderLayout.CENTER);
        content.add(progressPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }

    public void showSplash() {
        setVisible(true);
    }

    public void hideSplash() {
        setVisible(false);
        dispose();
    }
}
