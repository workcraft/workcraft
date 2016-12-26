package org.workcraft.plugins.transform;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.graph.commands.AbstractTransformationCommand;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
    public Collection<Node> collect(Model model) {
        Collection<Node> result = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            result.addAll(Hierarchy.getDescendantsOfType(model.getRoot(), VisualComponent.class));
            Collection<Node> selection = visualModel.getSelection();
            if (!selection.isEmpty()) {
                result.retainAll(selection);
            }
        }
        return result;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualComponent)) {
            VisualModel visualModel = (VisualModel) model;
            VisualComponent visualComponent = (VisualComponent) node;
            MathModel mathModel = visualModel.getMathModel();
            Node refComponent = visualComponent.getReferencedComponent();
            visualComponent.setLabel(mathModel.getName(refComponent));
        }
    }

}
