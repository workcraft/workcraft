package org.workcraft.gui.graph.editors;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.util.MessageUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class NameInplaceEditor extends AbstractInplaceEditor {
    private final VisualComponent component;
    private final VisualModel model;

    public NameInplaceEditor(GraphEditor editor, VisualComponent component) {
        super(editor, component);
        this.component = component;
        this.model = editor.getModel();
    }

    @Override
    public void processResult(String text) {
        WorkspaceEntry we = getEditor().getWorkspaceEntry();
        try {
            we.captureMemento();
            model.setMathName(component, text);
            we.saveMemento();
        } catch (ArgumentException e) {
            MessageUtils.showError(e.getMessage());
        }
    }

}
