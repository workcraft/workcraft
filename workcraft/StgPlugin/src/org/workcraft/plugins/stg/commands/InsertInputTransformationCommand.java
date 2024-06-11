package org.workcraft.plugins.stg.commands;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;

public final class InsertInputTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Insert input transitions into selected arcs";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Insert input transition";
    }

    @Override
    public VisualSignalTransition createTransition(VisualStg stg, Container container) {
        return stg.createVisualSignalTransition(null,
                Signal.Type.INPUT, SignalTransition.Direction.TOGGLE, container);
    }

}
