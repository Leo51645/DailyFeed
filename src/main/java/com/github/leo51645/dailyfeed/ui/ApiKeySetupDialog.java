package com.github.leo51645.dailyfeed.ui;

import com.google.genai.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiKeySetupDialog extends JDialog {

    private final JPasswordField geminiField = new JPasswordField(48);
    private final JPasswordField currentsField = new JPasswordField(48);
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton saveButton = new JButton("Speichern & Starten");

    private final String initialGeminiKey;
    private final String initialCurrentsKey;
    private boolean completed = false;
    private boolean keysChanged = false;

    public ApiKeySetupDialog() {
        this("", "", true);
    }

    public ApiKeySetupDialog(String existingGeminiKey, String existingCurrentsKey) {
        this(existingGeminiKey, existingCurrentsKey, false);
    }

    private ApiKeySetupDialog(String existingGeminiKey, String existingCurrentsKey, boolean exitOnClose) {
        super((Frame) null, "DailyFeed – API Keys einrichten", true);
        this.initialGeminiKey = existingGeminiKey;
        this.initialCurrentsKey = existingCurrentsKey;
        buildUI();
        if (!existingGeminiKey.isEmpty()) geminiField.setText(existingGeminiKey);
        if (!existingCurrentsKey.isEmpty()) currentsField.setText(existingCurrentsKey);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (exitOnClose) {
                    System.exit(0);
                } else {
                    dispose();
                }
            }
        });
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(30, 30, 30));
        content.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70), 1));

        JLabel title = new JLabel("DailyFeed", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(30, 20, 4, 20));

        JLabel subtitle = new JLabel("Bitte gib deine API Keys ein, um zu starten.", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(160, 160, 160));
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(30, 30, 30));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(30, 30, 30));
        formPanel.setBorder(BorderFactory.createEmptyBorder(0, 36, 10, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        styleField(geminiField);
        styleField(currentsField);

        addFormRow(formPanel, gbc, 0, "Gemini API Key:", geminiField);
        addFormRow(formPanel, gbc, 1, "Currents API Key:", currentsField);

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 100, 100));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 36, 0, 36));

        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.setBackground(new Color(100, 160, 255));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setOpaque(true);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> onSave());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(30, 30, 30));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 24, 0));
        buttonPanel.add(saveButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(30, 30, 30));
        southPanel.add(statusLabel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        content.add(titlePanel, BorderLayout.NORTH);
        content.add(formPanel, BorderLayout.CENTER);
        content.add(southPanel, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(saveButton);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JPasswordField field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(200, 200, 200));
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));

        gbc.gridy = row * 2;
        gbc.insets = new Insets(row == 0 ? 0 : 14, 0, 2, 0);
        panel.add(label, gbc);

        gbc.gridy = row * 2 + 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(buildFieldWithToggle(field), gbc);
    }

    private JPanel buildFieldWithToggle(JPasswordField field) {
        JButton toggle = new JButton("👁");
        toggle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        toggle.setBackground(new Color(65, 65, 65));
        toggle.setForeground(new Color(180, 180, 180));
        toggle.setFocusPainted(false);
        toggle.setBorderPainted(false);
        toggle.setOpaque(true);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setPreferredSize(new Dimension(36, field.getPreferredSize().height + 14));
        toggle.addActionListener(e -> {
            if (field.getEchoChar() == 0) {
                field.setEchoChar('•');
                toggle.setForeground(new Color(180, 180, 180));
            } else {
                field.setEchoChar((char) 0);
                toggle.setForeground(new Color(100, 160, 255));
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(new Color(50, 50, 50));
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(toggle, BorderLayout.EAST);
        return wrapper;
    }

    private void styleField(JPasswordField field) {
        field.setEchoChar('•');
        field.setBackground(new Color(50, 50, 50));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Monospaced", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    private void onSave() {
        String geminiKey = new String(geminiField.getPassword()).trim();
        String currentsKey = new String(currentsField.getPassword()).trim();

        if (geminiKey.isEmpty() || currentsKey.isEmpty()) {
            showStatus("Bitte beide API Keys eingeben.", true);
            return;
        }

        saveButton.setEnabled(false);
        showStatus("Keys werden geprüft...", false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                if (!validateGeminiKey(geminiKey)) return "Gemini API Key ist ungültig.";
                if (!validateCurrentsKey(currentsKey)) return "Currents API Key ist ungültig.";
                return null;
            }

            @Override
            protected void done() {
                try {
                    String error = get();
                    if (error != null) {
                        showStatus(error, true);
                        saveButton.setEnabled(true);
                    } else {
                        keysChanged = !geminiKey.equals(initialGeminiKey) || !currentsKey.equals(initialCurrentsKey);
                        writeKeys(geminiKey, currentsKey);
                        completed = true;
                        dispose();
                    }
                } catch (Exception e) {
                    showStatus("Fehler bei der Validierung.", true);
                    saveButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private boolean validateGeminiKey(String key) {
        try {
            Client client = Client.builder().apiKey(key).build();
            client.models.generateContent("gemini-2.5-flash", "ok", null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateCurrentsKey(String key) {
        try {
            HttpClient http = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.currentsapi.services/v2/search?apiKey=" + key))
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void writeKeys(String geminiKey, String currentsKey) {
        try {
            Files.writeString(Path.of(".env"),
                    "GEMINI_API_KEY=" + geminiKey + "\nCURRENTS_API_KEY=" + currentsKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setForeground(isError ? new Color(255, 100, 100) : new Color(160, 160, 160));
        statusLabel.setText(message);
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean keysChanged() {
        return keysChanged;
    }
}
