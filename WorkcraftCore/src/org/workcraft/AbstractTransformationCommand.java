package org.workcraft;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractTransformationCommand extends AbstractPromotedCommand implements MenuOrdering {

    @Override
    public String getSection() {
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
    public ModelEntry run(ModelEntry me) {
        VisualModel visualModel = WorkspaceUtils.getAs(me, VisualModel.class);
        Collection<Node> nodes = collect(visualModel);
        if (!nodes.isEmpty()) {
            final Framework framework = Framework.getInstance();
            final WorkspaceEntry we = framework.getWorkspaceEntry(me);
            we.saveMemento();
            transform(visualModel, nodes);
            framework.repaintCurrentEditor();
        }
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        run(we.getModelEntry());
        return we;
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
