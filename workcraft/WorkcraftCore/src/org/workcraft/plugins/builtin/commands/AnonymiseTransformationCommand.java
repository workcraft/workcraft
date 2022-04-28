package org.workcraft.plugins.builtin.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.math.MathModel;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class AnonymiseTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Anonymise";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we != null;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void transform(WorkspaceEntry we) {
        we.saveMemento();
        MathModel model = WorkspaceUtils.getAs(we, MathModel.class);
        model.anonymise();
        we.setChanged(true);
    }

}
