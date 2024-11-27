package org.strac.view.dialog;

import org.strac.service.GoogleDriveAuthenticatorService;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    private GoogleDriveAuthenticatorService authenticatorService;
    private JLabel authStatusLabel;
    private JButton authButton;

    public SettingsDialog(JFrame parent, GoogleDriveAuthenticatorService authenticatorService) {
        super(parent, "Settings", true);

        this.authenticatorService = authenticatorService;

        setLayout(new BorderLayout());
        setSize(400, 200);
        setLocationRelativeTo(parent);

        // Main panel for authentication status and button
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        authStatusLabel = new JLabel();
        authStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        authStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        authButton = new JButton();
        authButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        authButton.addActionListener(e -> handleAuthButtonClick());

        mainPanel.add(authStatusLabel, BorderLayout.CENTER);
        mainPanel.add(authButton, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        updateAuthStatus();

        // Stop the local server when the dialog is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                authenticatorService.stopLocalServer();
                dispose();
            }
        });
    }

    private void updateAuthStatus() {
        boolean isAuthenticated = authenticatorService.isAuthenticated();
        if (isAuthenticated) {
            authStatusLabel.setText("You are authenticated.");
            authButton.setText("Remove Authentication");
        } else {
            authStatusLabel.setText("You are not authenticated.");
            authButton.setText("Authenticate");
        }
    }

    private void handleAuthButtonClick() {
        if (authenticatorService.isAuthenticated()) {
            // Unauthenticate logic
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove authentication?",
                    "Confirm Unauthentication",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmation == JOptionPane.YES_OPTION) {
                authenticatorService.unauthenticate();
                JOptionPane.showMessageDialog(this, "Authentication removed.");
                updateAuthStatus();
            }
        } else {
            // Authenticate logic
            boolean success = authenticatorService.authenticate();
            if (success) {
                JOptionPane.showMessageDialog(this, "Authentication process started. Please complete it in the browser.");
                updateAuthStatus();
            } else {
                JOptionPane.showMessageDialog(this, "Authentication failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
