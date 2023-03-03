package org.workcraft.plugins.stg.commands;

import org.workcraft.dom.Container;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;

public final class InsertOutputTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Insert output transitions into selected arcs";
    }

    @Override
    public String getPopupName() {
        return "Insert output transition";
    }

    @Override
    public VisualSignalTransition createTransition(VisualStg stg, Container container) {
        return stg.createVisualSignalTransition(null,
                Signal.Type.OUTPUT, SignalTransition.Direction.TOGGLE, container);
    }

}
