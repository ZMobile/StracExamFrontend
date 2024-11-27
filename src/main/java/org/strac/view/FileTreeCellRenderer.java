package org.strac.view;

import org.strac.model.DriveFile;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Set;

public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JCheckBox checkBox = new JCheckBox();
    private final JLabel iconLabel = new JLabel();
    private final JLabel textLabel = new JLabel();
    private final Set<DriveFile> selectedFiles; // Reference to selected files

    public FileTreeCellRenderer(Set<DriveFile> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // Reset the panel
        panel.removeAll();

        // Default rendering for styles (fonts, colors, etc.)
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof DriveFile) {
                DriveFile file = (DriveFile) userObject;

                // Set checkbox state based on selectedFiles
                checkBox.setSelected(selectedFiles.contains(file));

                // Set icon
                Icon icon = getIconForNode(leaf, expanded);
                iconLabel.setIcon(icon);

                // Set text
                textLabel.setText(file.getName());

                // Apply selection colors
                panel.setBackground(tree.getBackground());
                checkBox.setBackground(tree.getBackground());
                iconLabel.setBackground(tree.getBackground());
                textLabel.setBackground(tree.getBackground());
                textLabel.setForeground(tree.getForeground());

                // Add components to panel
                panel.add(checkBox, BorderLayout.WEST); // Checkbox on the left
                panel.add(iconLabel, BorderLayout.CENTER); // Icon next to checkbox
                panel.add(textLabel, BorderLayout.EAST); // Text next to icon

                return panel;
            }
        }

        // Default rendering for non-DriveFile nodes
        return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    private Icon getIconForNode(boolean leaf, boolean expanded) {
        // Return appropriate icons (use DefaultTreeCellRenderer's logic)
        if (leaf) {
            return getLeafIcon();
        } else if (expanded) {
            return getOpenIcon();
        } else {
            return getClosedIcon();
        }
    }
}
