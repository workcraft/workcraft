package org.workcraft.gui.graph.editors;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.workspace.WorkspaceEntry;

public class LabelInplaceEditor extends AbstractInplaceEditor {
    private final VisualComponent component;

    public LabelInplaceEditor(GraphEditor editor, VisualComponent component) {
        super(editor, component);
        this.component = component;
    }

    @Override
    public void processResult(String text) {
        WorkspaceEntry we = getEditor().getWorkspaceEntry();
        we.saveMemento();
        component.setLabel(text);
    }

}
