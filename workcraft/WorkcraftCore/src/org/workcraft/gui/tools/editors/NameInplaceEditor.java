package org.workcraft.gui.tools.editors;

import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class NameInplaceEditor extends AbstractInplaceEditor {

    private final VisualComponent component;
    private final VisualModel model;
    private final boolean validate;

    public NameInplaceEditor(GraphEditor editor, VisualComponent component) {
        this(editor, component, false);
    }
    public NameInplaceEditor(GraphEditor editor, VisualComponent component, boolean validate) {
        super(editor, component);
        this.component = component;
        this.model = editor.getModel();
        this.validate = validate;
    }

    @Override
    public void processResult(String text) {
        try {
            if (validate) {
                Identifier.validate(text);
            }
            WorkspaceEntry we = getEditor().getWorkspaceEntry();
            we.captureMemento();
            model.setMathName(component, text);
            we.saveMemento();
        } catch (ArgumentException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

}
