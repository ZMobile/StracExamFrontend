package org.strac.view;

import org.strac.model.DriveFile;
import org.strac.service.GoogleDriveAuthenticatorService;
import org.strac.service.GoogleDriveService;
import org.strac.view.dialog.DeleteDialog;
import org.strac.view.dialog.DownloadDialog;
import org.strac.view.dialog.SettingsDialog;
import org.strac.view.dialog.UploadDialog;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoogleDriveFileViewer extends JFrame {
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private Set<DriveFile> selectedFiles = new HashSet<>();
    private Rectangle dragSelectionRectangle = null;
    private Point dragStartPoint = null;
    private JToolBar toolbar;
    private TreePath lastSelectedPath = null; // Keep track of the last selected path

    private GoogleDriveService driveService;
    private GoogleDriveAuthenticatorService authenticatorService;

    public GoogleDriveFileViewer(GoogleDriveService driveService,
                                 GoogleDriveAuthenticatorService authenticatorService) {
        this.driveService = driveService;
        this.authenticatorService = authenticatorService;

        setTitle("Google Drive File Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeToolbar();
        initializeFileTree();
        initializeContextMenu();
        JScrollPane scrollPane = new JScrollPane(fileTree);
        scrollPane.addMouseListener(new GlobalMouseAdapter());
        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Register a global mouse listener to clear the drag selection rectangle
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent mouseEvent) {
                if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED) {
                    if (dragSelectionRectangle != null) {
                        selectNodesInRectangle(dragSelectionRectangle);
                    }
                    dragStartPoint = null;
                    dragSelectionRectangle = null; // Clear the rectangle
                    repaint(); // Refresh the UI
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    private void initializeToolbar() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS)); // Horizontal layout

        // Modern FlatLaf buttons
        JButton downloadButton = createModernButton("Download");
        downloadButton.addActionListener(e -> downloadSelectedFiles());

        JButton deleteButton = createModernButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedFiles());

        JButton uploadButton = createModernButton("Upload");
        uploadButton.addActionListener(e -> uploadFileToSelectedFolder());

        JButton settingsButton = createModernButton("Settings");
        settingsButton.addActionListener(e -> showSettingsDialog());

        // Add buttons to the toolbar
        toolbar.add(downloadButton);
        toolbar.add(deleteButton);
        toolbar.add(uploadButton);

        // Add a horizontal glue to push the settings button to the right
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(settingsButton);
    }

    private JButton createModernButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void initializeFileTree() {
        // Set root node with a DriveFile object
        DriveFile rootDriveFile = new DriveFile("root", "Google Drive", "application/vnd.google-apps.folder", null, "Root Folder");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootDriveFile);

        // Add a placeholder to make the root expandable
        root.add(new DefaultMutableTreeNode("Loading..."));

        treeModel = new DefaultTreeModel(root);
        fileTree = new JTree(treeModel) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw the drag selection rectangle if it exists
                if (dragSelectionRectangle != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(128, 128, 255, 64)); // Semi-transparent blue fill
                    g2.fill(dragSelectionRectangle);
                    g2.setColor(Color.BLUE); // Outline color
                    g2.draw(dragSelectionRectangle);
                }
            }
        };

        fileTree.setCellRenderer(new FileTreeCellRenderer(selectedFiles));
        fileTree.setEditable(false); // Disable editing to prevent renaming
        fileTree.setRootVisible(true); // Show the root node

        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    handleLeftClick(e);
                }
                // Check if the event is a right click
                else if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClick(e);
                }
            }

            private void handleLeftClick(MouseEvent e) {
                TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());

                if (path != null) {
                    Rectangle checkBoxBounds = getCheckBoxBounds(fileTree, path);

                    if (checkBoxBounds != null && checkBoxBounds.contains(e.getPoint())) {
                        // Checkbox click logic remains the same
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof DriveFile) {
                            DriveFile file = (DriveFile) node.getUserObject();
                            if (selectedFiles.contains(file)) {
                                selectedFiles.remove(file); // Unselect only this file
                            } else {
                                selectedFiles.add(file); // Select only this file
                            }
                        }
                    } else {
                        if (e.isShiftDown() && lastSelectedPath != null) {
                            // Standard shift-click selection behavior
                            TreePath[] paths = getPathsBetween(lastSelectedPath, path);
                            for (TreePath p : paths) {
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
                                if (node.getUserObject() instanceof DriveFile) {
                                    DriveFile file = (DriveFile) node.getUserObject();
                                    /*if (selectedFiles.contains(file)) {
                                        selectedFiles.remove(file); // Toggle selection
                                    } else {*/
                                        selectedFiles.add(file);
                                    //}
                                }
                            }
                        } else {
                            // Standard single-click selection behavior
                            if (!e.isShiftDown()) {
                                selectedFiles.clear(); // Clear selection if Shift is not held
                            }
                            lastSelectedPath = path; // Update last selected path

                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                            if (node.getUserObject() instanceof DriveFile) {
                                DriveFile file = (DriveFile) node.getUserObject();
                                selectedFiles.add(file); // Select the clicked file
                            }
                        }
                    }
                } else if (!e.isShiftDown()) {
                    selectedFiles.clear(); // Clear all selections if clicked outside any node
                }

                fileTree.repaint(); // Refresh the tree display
            }

            private void handleRightClick(MouseEvent e) {
                TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    fileTree.setSelectionPath(path); // Ensure the right-clicked item is selected
                    contextMenu.show(fileTree, e.getX(), e.getY());
                }
            }
        });

        fileTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                loadFiles(node); // Load files when a node is expanded
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
                // No action needed
            }
        });

        fileTree.addMouseListener(new DragSelectionMouseAdapter());
        fileTree.addMouseMotionListener(new DragSelectionMouseMotionAdapter());

        // Trigger loading files for the root node
        loadFiles(root);
    }

    private class DragSelectionMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                dragStartPoint = e.getPoint();
                dragSelectionRectangle = null;
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (dragSelectionRectangle != null) {
                    selectNodesInRectangle(dragSelectionRectangle);
                }
                dragStartPoint = null;
                dragSelectionRectangle = null;
                repaint();
            }
        }
    }

    private class DragSelectionMouseMotionAdapter extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && dragStartPoint != null) {
                Point dragEndPoint = e.getPoint();
                dragSelectionRectangle = new Rectangle(
                        Math.min(dragStartPoint.x, dragEndPoint.x),
                        Math.min(dragStartPoint.y, dragEndPoint.y),
                        Math.abs(dragStartPoint.x - dragEndPoint.x),
                        Math.abs(dragStartPoint.y - dragEndPoint.y)
                );
                repaint();
            }
        }
    }

    private JPopupMenu contextMenu;

    private void initializeContextMenu() {
        contextMenu = new JPopupMenu();

        JMenuItem downloadMenuItem = new JMenuItem("Download");
        downloadMenuItem.addActionListener(e -> downloadSelectedFiles());
        contextMenu.add(downloadMenuItem);

        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(e -> deleteSelectedFiles());
        contextMenu.add(deleteMenuItem);
    }

    /**
     * Gets all TreePaths between two nodes in the tree.
     */
    private TreePath[] getPathsBetween(TreePath path1, TreePath path2) {
        int row1 = fileTree.getRowForPath(path1);
        int row2 = fileTree.getRowForPath(path2);
        int minRow = Math.min(row1, row2);
        int maxRow = Math.max(row1, row2);

        TreePath[] paths = new TreePath[maxRow - minRow + 1];
        for (int i = minRow; i <= maxRow; i++) {
            paths[i - minRow] = fileTree.getPathForRow(i);
        }
        return paths;
    }

    private Rectangle getCheckBoxBounds(JTree tree, TreePath path) {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        Component component = renderer.getTreeCellRendererComponent(
                tree, path.getLastPathComponent(), false, false, false, 0, false);

        if (component instanceof JPanel panel) {
            JCheckBox checkBox = (JCheckBox) panel.getComponent(0); // Assume checkbox is the first component
            Rectangle bounds = tree.getPathBounds(path);
            if (bounds != null) {
                bounds.width = checkBox.getPreferredSize().width;
                return bounds;
            }
        }
        return null;
    }

    private void loadFiles(DefaultMutableTreeNode parentNode) {
        SwingWorker<Void, DriveFile> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                String parentId = parentNode.getUserObject() instanceof DriveFile
                        ? ((DriveFile) parentNode.getUserObject()).getId()
                        : "root";

                List<DriveFile> files = driveService.listFiles(parentId); // Call the service
                for (DriveFile file : files) {
                    publish(file);
                }
                return null;
            }

            @Override
            protected void process(List<DriveFile> files) {
                parentNode.removeAllChildren(); // Clear previous children
                for (DriveFile file : files) {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
                    parentNode.add(childNode);
                    if ("application/vnd.google-apps.folder".equals(file.getMimeType())) {
                        childNode.add(new DefaultMutableTreeNode("Loading..."));
                    }
                }
                treeModel.reload(parentNode); // Refresh the tree model
            }
        };
        worker.execute();
    }

    private void downloadSelectedFiles() {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files selected for download.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DownloadDialog downloadDialog = new DownloadDialog(this, new ArrayList<>(selectedFiles), driveService);
        downloadDialog.setVisible(true);
    }

    private void deleteSelectedFiles() {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files selected for deletion.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DeleteDialog deleteDialog = new DeleteDialog(this, new ArrayList<>(selectedFiles), driveService);
        deleteDialog.setVisible(true);
    }

    private void uploadFileToSelectedFolder() {
        UploadDialog uploadDialog = new UploadDialog(this, driveService, treeModel);
        uploadDialog.setVisible(true);
    }

    private void showSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog(this, authenticatorService);
        settingsDialog.setVisible(true);
    }

    private void selectNodesInRectangle(Rectangle selectionRect) {
        int rowCount = fileTree.getRowCount();
        Set<DriveFile> filesToToggle = new HashSet<>();

        for (int row = 0; row < rowCount; row++) {
            TreePath path = fileTree.getPathForRow(row);
            Rectangle pathBounds = fileTree.getPathBounds(path);

            if (selectionRect.intersects(pathBounds)) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node.getUserObject() instanceof DriveFile) {
                    DriveFile file = (DriveFile) node.getUserObject();
                    if (selectedFiles.contains(file)) {
                        filesToToggle.add(file); // Mark for unselecting
                    } else {
                        selectedFiles.add(file); // Select new files
                    }
                }
            }
        }

        // Unselect files that were toggled
        for (DriveFile file : filesToToggle) {
            selectedFiles.remove(file);
        }
    }


    private class GlobalMouseAdapter extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                dragSelectionRectangle = null;
                repaint();
            }
        }
    }
}