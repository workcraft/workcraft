package org.workcraft.plugins.stg.commands;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;

public final class InsertDummyTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Insert dummies into selected arcs";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Insert dummy";
    }

    @Override
    public VisualDummyTransition createTransition(VisualStg stg, Container container) {
        return stg.createVisualDummyTransition(null, container);
    }

}
