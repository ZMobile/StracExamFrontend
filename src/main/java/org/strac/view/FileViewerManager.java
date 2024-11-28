package org.strac.view;

import org.strac.model.DriveFile;
import org.strac.service.GoogleDriveAuthenticatorService;
import org.strac.service.GoogleDriveService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileViewerManager {
    private JFrame parentComponent;
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private JToolBar toolbar;
    private Set<DriveFile> selectedFiles = new HashSet<>();
    private GoogleDriveService driveService;
    private GoogleDriveAuthenticatorService authenticatorService;

    public FileViewerManager() {

    }

    public FileViewerManager(JFrame parentComponent, JTree fileTree, DefaultTreeModel treeModel, JToolBar toolbar, Set<DriveFile> selectedFiles, GoogleDriveService driveService, GoogleDriveAuthenticatorService authenticatorService) {
        this.parentComponent = parentComponent;
        this.fileTree = fileTree;
        this.treeModel = treeModel;
        this.toolbar = toolbar;
        this.selectedFiles = selectedFiles;
        this.driveService = driveService;
        this.authenticatorService = authenticatorService;
    }

    public void initializeFileViewerManager(JFrame parentComponent, JTree fileTree, DefaultTreeModel treeModel, JToolBar toolbar, Set<DriveFile> selectedFiles, GoogleDriveService driveService, GoogleDriveAuthenticatorService authenticatorService) {
        this.parentComponent = parentComponent;
        this.fileTree = fileTree;
        this.treeModel = treeModel;
        this.toolbar = toolbar;
        this.selectedFiles = selectedFiles;
        this.driveService = driveService;
        this.authenticatorService = authenticatorService;
    }

    public void refreshFileTree() {
        if (!authenticatorService.isAuthenticated()) {
            // Clear the file tree if not authenticated
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
            root.removeAllChildren();
            treeModel.reload();
            selectedFiles.clear();
            return;
        }

        // Reload the file tree if authenticated
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();
        root.add(new DefaultMutableTreeNode("Loading..."));
        treeModel.reload();
        loadFiles(root);
    }

    public void refreshFolder(DefaultMutableTreeNode parentNode) {
        if (!authenticatorService.isAuthenticated()) {
            JOptionPane.showMessageDialog(parentComponent, "You need to authenticate first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        parentNode.removeAllChildren();
        parentNode.add(new DefaultMutableTreeNode("Loading..."));
        treeModel.reload(parentNode);
        loadFiles(parentNode);
    }

    public void refreshFolder(String fileId) {
        if (!authenticatorService.isAuthenticated()) {
            JOptionPane.showMessageDialog(parentComponent, "You need to authenticate first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        DefaultMutableTreeNode targetNode = findNodeById(root, fileId);

        if (targetNode != null) {
            refreshFolder(targetNode);
        }/* else {
            //JOptionPane.showMessageDialog(parentComponent, "Folder not found in currently visible nodes.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }*/
    }

    private DefaultMutableTreeNode findNodeById(DefaultMutableTreeNode node, String fileId) {
        Object userObject = node.getUserObject();

        if (userObject instanceof DriveFile driveFile && fileId.equals(driveFile.getId())) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            DefaultMutableTreeNode result = findNodeById(childNode, fileId);
            if (result != null) {
                return result;
            }
        }

        return null; // Node with the given fileId not found
    }

    public void deleteFilesRecursively(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        deleteFilesRecursively(root, fileIds);

        // Refresh the entire tree model to ensure changes are visible
        treeModel.reload(root);
        fileTree.repaint();
    }

    public void deleteFilesRecursively(DefaultMutableTreeNode node, List<String> fileIds) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            Object userObject = childNode.getUserObject();

            if (userObject instanceof DriveFile driveFile) {
                if (fileIds.contains(driveFile.getId())) {
                    node.remove(childNode);
                    i--; // Adjust the index after removal
                }
            }

            // Recursively check child nodes
            deleteFilesRecursively(childNode, fileIds);
        }
    }

    public void loadFiles(DefaultMutableTreeNode parentNode) {
        SwingWorker<Void, DriveFile> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                DriveFile parent = parentNode.getUserObject() instanceof DriveFile
                        ? ((DriveFile) parentNode.getUserObject())
                        : new DriveFile("root", "Google Drive", "application/vnd.google-apps.folder", null, "Google Drive");
                System.out.println("Loading files for parent: " + parent.getName());
                List<DriveFile> files = driveService.listFiles(parent); // Call the service
                System.out.println("Done loading files for parent: " + parent.getName());
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
}
