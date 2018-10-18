package org.workcraft.plugins.transform;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class CopyLabelTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Copy unique names into labels (selected or all)";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> result = new HashSet<>();
        result.addAll(Hierarchy.getDescendantsOfType(model.getRoot(), VisualComponent.class));
        Collection<? extends VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            result.retainAll(selection);
        }
        return result;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if (node instanceof VisualComponent) {
            VisualComponent component = (VisualComponent) node;
            MathModel mathModel = model.getMathModel();
            Node refComponent = component.getReferencedComponent();
            component.setLabel(mathModel.getName(refComponent));
        }
    }

}
