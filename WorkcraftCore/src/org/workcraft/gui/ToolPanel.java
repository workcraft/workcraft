package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;

@SuppressWarnings("serial")
public class ToolPanel extends JPanel {

    private final JScrollPane content;

    public ToolPanel() {
        super(new BorderLayout());
        content = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        content.setBorder(null);
        content.setViewportView(new DisabledPanel());
        add(content, BorderLayout.CENTER);
    }

    public boolean setTool(GraphEditorTool tool, GraphEditor editor) {
        boolean result = true;
        JPanel panel = new JPanel();
        if (tool != null) {
            tool.updatePanel(panel, editor);
        }
        if (panel.getComponentCount() == 0) {
            panel = new DisabledPanel();
        }
        content.setViewportView(panel);
        content.revalidate();
        return result;
    }

}
