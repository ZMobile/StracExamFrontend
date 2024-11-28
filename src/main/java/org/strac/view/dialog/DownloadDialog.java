package org.strac.view.dialog;

import org.strac.model.DriveFile;
import org.strac.service.GoogleDriveService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadDialog extends JDialog {
    private List<DriveFile> filesToDownload;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JTextField destinationPathField;
    private GoogleDriveService driveService;

    public DownloadDialog(JFrame parent, List<DriveFile> selectedFiles, GoogleDriveService driveService) {
        super(parent, "Download Files", true);

        this.filesToDownload = consolidateFiles(selectedFiles);
        this.driveService = driveService;

        setLayout(new BorderLayout());
        setSize(600, 400);
        setLocationRelativeTo(parent);

        // Top panel: Destination path selector
        JPanel destinationPanel = new JPanel(new BorderLayout(5, 5));
        JLabel destinationLabel = new JLabel("Destination:");
        destinationPathField = new JTextField(System.getProperty("user.home"));
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(this::browseForDestination);

        destinationPanel.add(destinationLabel, BorderLayout.WEST);
        destinationPanel.add(destinationPathField, BorderLayout.CENTER);
        destinationPanel.add(browseButton, BorderLayout.EAST);

        // Center panel: File list table
        JPanel centerPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Type", "File Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        fileTable = new JTable(tableModel);
        fileTable.setRowHeight(30);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add custom renderer for the Type (icon) column
        fileTable.getColumn("Type").setCellRenderer(new IconCellRenderer());

        // Adjust column widths
        TableColumn typeColumn = fileTable.getColumn("Type");
        typeColumn.setMaxWidth(50); // Limit icon column width

        refreshFileTable();

        JScrollPane scrollPane = new JScrollPane(fileTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel: Confirm/Cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton confirmButton = new JButton("Download");

        cancelButton.addActionListener(e -> dispose());
        confirmButton.addActionListener(e -> confirmDownload());

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        // Add panels to the dialog
        add(destinationPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void browseForDestination(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            destinationPathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    private List<DriveFile> consolidateFiles(List<DriveFile> selectedFiles) {
        List<DriveFile> consolidated = new ArrayList<>();
        for (DriveFile file : selectedFiles) {
            boolean isSuperFolder = consolidated.stream().anyMatch(parent -> file.getPath().startsWith(parent.getPath()));
            if (!isSuperFolder) {
                consolidated.removeIf(parent -> parent.getPath().startsWith(file.getPath()));
                consolidated.add(file);
            }
        }
        return consolidated;
    }

    private void refreshFileTable() {
        tableModel.setRowCount(0); // Clear the table
        for (DriveFile file : filesToDownload) {
            Icon icon = file.getMimeType().equals("application/vnd.google-apps.folder")
                    ? UIManager.getIcon("FileView.directoryIcon")
                    : UIManager.getIcon("FileView.fileIcon");
            tableModel.addRow(new Object[]{icon, file.getName()});
        }
    }

    private void confirmDownload() {
        String destinationPath = destinationPathField.getText();
        if (destinationPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a destination folder.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Perform the download action
        for (DriveFile file : filesToDownload) {
            if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                driveService.downloadFolder(file, destinationPath);
            } else {
                driveService.downloadFile(file, destinationPath);
            }
        }

        JOptionPane.showMessageDialog(this, "Download Successful!");
        dispose();
    }

    // Custom cell renderer for icons
    private static class IconCellRenderer extends JLabel implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Icon) {
                setIcon((Icon) value);
                setHorizontalAlignment(CENTER);
            } else {
                setIcon(null);
            }
            return this;
        }
    }
}
