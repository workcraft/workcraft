package org.workcraft.gui.graph.tools;

import org.workcraft.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.InvalidConnectionException;

public abstract class AbstractContractTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Contract selected nodes";
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualComponent)) {
            VisualModel visualModel = (VisualModel) model;
            for (Node pred: model.getPreset(node)) {
                for (Node succ: model.getPostset(node)) {
                    try {
                        visualModel.connect(pred, succ);
                    } catch (InvalidConnectionException e) {
                        e.printStackTrace();
                    }
                }
            }
            visualModel.deleteSelection();
        }
    }

}
