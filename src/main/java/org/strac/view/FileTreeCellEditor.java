package org.strac.view;

import org.strac.model.DriveFile;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class FileTreeCellEditor extends DefaultTreeCellEditor {
    private final JCheckBox checkBox = new JCheckBox();

    public FileTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                boolean expanded, boolean leaf, int row) {
        Component c = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof DriveFile) {
                DriveFile file = (DriveFile) userObject;
                checkBox.setText(file.getName());
                checkBox.setSelected(isSelected); // Reflect current selection state
                return checkBox;
            }
        }

        return c; // Default behavior for other nodes
    }
}
