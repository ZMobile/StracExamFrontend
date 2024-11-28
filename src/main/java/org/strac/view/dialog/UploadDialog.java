package org.strac.view.dialog;

import org.strac.model.DriveFile;
import org.strac.service.GoogleDriveService;
import org.strac.view.FileViewerManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadDialog extends JDialog {
    private List<File> filesToUpload = new ArrayList<>();
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JTextField destinationPathField;
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private GoogleDriveService driveService;
    private FileViewerManager fileViewerManager;
    private DriveFile selectedDestinationFolder;

    // Constructor for general use (no predefined destination or file list)
    public UploadDialog(JFrame parent, GoogleDriveService driveService, FileViewerManager fileViewerManager, DefaultTreeModel treeModel) {
        this(parent, driveService, fileViewerManager, treeModel, new ArrayList<>(), null);
    }

    // Constructor for predefined destination and file list
    public UploadDialog(JFrame parent, GoogleDriveService driveService, FileViewerManager fileViewerManager, DefaultTreeModel treeModel,
                        List<File> predefinedFiles, DriveFile predefinedDestinationFolder) {
        super(parent, "Upload Files", true);

        this.driveService = driveService;
        this.fileViewerManager = fileViewerManager;
        this.treeModel = treeModel;
        if (predefinedFiles != null) {
            this.filesToUpload.addAll(predefinedFiles);
        }
        this.selectedDestinationFolder = predefinedDestinationFolder;

        setLayout(new BorderLayout());
        setSize(800, 500);
        setLocationRelativeTo(parent);

        initializeUI();
        if (predefinedDestinationFolder != null) {
            destinationPathField.setText(predefinedDestinationFolder.getName());
        }
        populateFileTable();
    }

    private void initializeUI() {
        // Top panel: Destination path selector
        JPanel destinationPanel = new JPanel(new BorderLayout(5, 5));
        JLabel destinationLabel = new JLabel("Destination Folder:");
        destinationPathField = new JTextField("Select a folder...");
        destinationPathField.setEditable(false);

        destinationPanel.add(destinationLabel, BorderLayout.WEST);
        destinationPanel.add(destinationPathField, BorderLayout.CENTER);

        // Center panel: File list and file tree
        JSplitPane centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerPanel.setResizeWeight(0.5);

        // File Table
        JPanel fileListPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"File/Folder Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileTable = new JTable(tableModel);
        fileTable.setRowHeight(30);

        JScrollPane fileScrollPane = new JScrollPane(fileTable);
        JButton addFileButton = new JButton("Add Files or Folders");
        addFileButton.addActionListener(this::addFilesOrFolders);

        fileListPanel.setTransferHandler(new FileDropHandler());
        fileTable.setTransferHandler(new FileDropHandler());

        fileListPanel.add(fileScrollPane, BorderLayout.CENTER);
        fileListPanel.add(addFileButton, BorderLayout.SOUTH);
        centerPanel.setLeftComponent(fileListPanel);

        // File Tree
        JPanel fileTreePanel = new JPanel(new BorderLayout());
        fileTree = new JTree(treeModel);
        fileTree.setRootVisible(true);
        fileTree.addTreeSelectionListener(e -> updateSelectedFolder());
        fileTree.addTreeWillExpandListener(new FileTreeExpandListener(driveService));

        JScrollPane treeScrollPane = new JScrollPane(fileTree);

        fileTreePanel.add(treeScrollPane, BorderLayout.CENTER);
        centerPanel.setRightComponent(fileTreePanel);

        // Bottom panel: Confirm/Cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmButton = new JButton("Upload");
        JButton cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(e -> confirmUpload());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        // Add panels to the dialog
        add(destinationPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateFileTable() {
        for (File file : filesToUpload) {
            tableModel.addRow(new Object[]{file.getName()});
        }
    }

    private void addFilesOrFolders(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                if (!filesToUpload.contains(file)) {
                    filesToUpload.add(file);
                    tableModel.addRow(new Object[]{file.getName()});
                }
            }
        }
    }

    private void updateSelectedFolder() {
        TreePath selectedPath = fileTree.getSelectionPath();
        if (selectedPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            if (node.getUserObject() instanceof DriveFile driveFile && driveFile.getMimeType().equals("application/vnd.google-apps.folder")) {
                selectedDestinationFolder = driveFile;
                destinationPathField.setText(driveFile.getName());
            }
        }
    }

    private void confirmUpload() {
        if (filesToUpload.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files or folders selected for upload.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedDestinationFolder == null) {
            JOptionPane.showMessageDialog(this, "No destination folder selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Perform the upload action using the drive service
        for (File file : filesToUpload) {
            System.out.println("Uploading: " + file.getName() + " to " + selectedDestinationFolder.getName());
            driveService.uploadFile(selectedDestinationFolder.getId(), file.getPath());
        }

        fileViewerManager.refreshFolder(selectedDestinationFolder.getId());
        JOptionPane.showMessageDialog(this, "Upload Successful!");
        dispose();
    }

    private class FileTreeExpandListener implements javax.swing.event.TreeWillExpandListener {
        private final GoogleDriveService driveService;

        public FileTreeExpandListener(GoogleDriveService driveService) {
            this.driveService = driveService;
        }

        @Override
        public void treeWillExpand(javax.swing.event.TreeExpansionEvent event) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
            if (node.getChildCount() == 1 && node.getFirstChild().toString().equals("Loading...")) {
                node.removeAllChildren();
                DriveFile driveFile = (DriveFile) node.getUserObject();
                List<DriveFile> children = driveService.listFiles(driveFile.getId());
                for (DriveFile child : children) {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                    if (child.getMimeType() != null && child.getMimeType().equals("application/vnd.google-apps.folder")) {
                        childNode.add(new DefaultMutableTreeNode("Loading..."));
                    }
                    node.add(childNode);
                }
                ((DefaultTreeModel) fileTree.getModel()).reload(node);
            }
        }

        @Override
        public void treeWillCollapse(javax.swing.event.TreeExpansionEvent event) {
            // No action needed
        }
    }

    private class FileDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            // Only accept file drops
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                // Get the dropped files
                List<File> droppedFiles = (List<File>) support.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);

                for (File file : droppedFiles) {
                    if (!filesToUpload.contains(file)) {
                        filesToUpload.add(file);
                        tableModel.addRow(new Object[]{file.getName()});
                    }
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(UploadDialog.this, "Failed to process dropped files.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }
}
