package com.github.leo51645.dailyfeed.ui;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

    private final JProgressBar progressBar;
    private final Timer animationTimer;

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
        progressBar.setPreferredSize(new Dimension(0, 3));
        progressBar.setBorderPainted(false);
        progressBar.setBackground(new Color(50, 50, 50));
        progressBar.setForeground(new Color(90, 150, 255));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(new Color(30, 30, 30));
        textPanel.add(title, BorderLayout.NORTH);
        textPanel.add(subtitle, BorderLayout.CENTER);

        content.add(textPanel, BorderLayout.CENTER);
        content.add(progressBar, BorderLayout.SOUTH);

        setContentPane(content);

        animationTimer = new Timer(16, e -> progressBar.repaint());
    }

    public void showSplash() {
        animationTimer.start();
        setVisible(true);
    }

    public void hideSplash() {
        animationTimer.stop();
        setVisible(false);
        dispose();
    }
}
