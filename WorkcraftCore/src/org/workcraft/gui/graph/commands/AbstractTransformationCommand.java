package org.workcraft.gui.graph.commands;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.MenuOrdering;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractTransformationCommand implements ExecutableCommand, MenuOrdering {

    @Override
    public final String getSection() {
        return "!   Transformations";  // 3 spaces - positions 2nd
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        VisualModel visualModel = WorkspaceUtils.getAs(we, VisualModel.class);
        Collection<Node> nodes = collect(visualModel);
        if (!nodes.isEmpty()) {
            we.saveMemento();
            transform(visualModel, nodes);
        }
        return we;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        execute(we);
    }

    public Collection<Node> collect(Model model) {
        Collection<Node> result = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            result.addAll(visualModel.getSelection());
        }
        return result;
    }

    public void transform(Model model, Collection<Node> nodes) {
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            for (Node node: nodes) {
                transform(model, node);
            }
            visualModel.selectNone();
        }
    }

    public abstract void transform(Model model, Node node);

}
