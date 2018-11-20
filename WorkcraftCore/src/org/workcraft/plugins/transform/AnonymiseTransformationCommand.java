package org.workcraft.plugins.transform;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class AnonymiseTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Anonymise";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }


    @Override
    public Void execute(WorkspaceEntry we) {
        we.saveMemento();
        MathModel model = WorkspaceUtils.getAs(we, MathModel.class);
        model.anonymise();
        we.setChanged(true);
        return null;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
    }

}
