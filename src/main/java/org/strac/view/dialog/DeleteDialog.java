package org.strac.view.dialog;

import org.strac.model.DriveFile;
import org.strac.service.GoogleDriveService;
import org.strac.view.FileViewerManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DeleteDialog extends JDialog {
    private List<DriveFile> filesToDelete;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private GoogleDriveService driveService;
    private FileViewerManager fileViewerManager;

    public DeleteDialog(JFrame parent, List<DriveFile> selectedFiles, GoogleDriveService driveService, FileViewerManager fileViewerManager) {
        super(parent, "Delete the selected files?", true);

        this.driveService = driveService;
        this.fileViewerManager = fileViewerManager;
        this.filesToDelete = consolidateFiles(selectedFiles);

        setLayout(new BorderLayout());
        setSize(600, 400);
        setLocationRelativeTo(parent);

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
        JButton confirmButton = new JButton("Delete");

        cancelButton.addActionListener(e -> dispose());
        confirmButton.addActionListener(e -> confirmDeletion());

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        // Add panels to the dialog
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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
        for (DriveFile file : filesToDelete) {
            Icon icon = file.getMimeType().equals("application/vnd.google-apps.folder")
                    ? UIManager.getIcon("FileView.directoryIcon")
                    : UIManager.getIcon("FileView.fileIcon");
            tableModel.addRow(new Object[]{icon, file.getName()});
        }
    }

    private void confirmDeletion() {
        // Perform the deletion action
        List<String> fileIds = new ArrayList<>();
        for (DriveFile file : filesToDelete) {
            System.out.println("Deleting: " + file.getName());
            driveService.deleteFile(file.getId());
            fileIds.add(file.getId());
        }
        fileViewerManager.deleteFilesRecursively(fileIds);

        JOptionPane.showMessageDialog(this, "Files deleted successfully!");
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
