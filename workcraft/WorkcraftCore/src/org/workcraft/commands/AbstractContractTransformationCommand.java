package org.workcraft.commands;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;

public abstract class AbstractContractTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Contract selected nodes";
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        for (VisualNode pred : model.getPreset(node)) {
            for (VisualNode succ : model.getPostset(node)) {
                try {
                    model.connect(pred, succ);
                } catch (InvalidConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
        model.deleteSelection();
    }

}
