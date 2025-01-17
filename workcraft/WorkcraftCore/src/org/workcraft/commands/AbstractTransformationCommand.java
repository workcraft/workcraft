package org.workcraft.commands;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.utils.CommandUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractTransformationCommand implements ScriptableCommand<Void>, MenuOrdering {

    private static final String SECTION_TITLE = CommandUtils.makePromotedSectionTitle("Transformation", 2);

    @Override
    public final String getSection() {
        return SECTION_TITLE;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        // Run synchronously (blocking the editor) as model is changed.
        execute(we);
    }

    @Override
    public final Void execute(WorkspaceEntry we) {
        transform(we);
        return null;
    }

    public void transform(WorkspaceEntry we) {
        VisualModel visualModel = WorkspaceUtils.getAs(we, VisualModel.class);
        Collection<? extends VisualNode> nodes = collectNodes(visualModel);
        if (nodes.isEmpty()) {
            logNoNodesWarning(visualModel);
        } else {
            we.saveMemento();
            transformNodes(visualModel, nodes);
            we.setChanged(true);
        }
    }

    public void logNoNodesWarning(VisualModel model) {
        // Skip warning
    }

    public Collection<? extends VisualNode> collectNodes(VisualModel model) {
        return new HashSet<>(model.getSelection());
    }

    public void transformNodes(VisualModel model, Collection<? extends VisualNode> nodes) {
        model.selectNone();
        for (VisualNode node: nodes) {
            transformNode(model, node);
        }
    }

    public void transformNode(VisualModel model, VisualNode node) {
    }

}
