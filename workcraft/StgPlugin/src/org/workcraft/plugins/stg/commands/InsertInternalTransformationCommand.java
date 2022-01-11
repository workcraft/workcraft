package org.workcraft.plugins.stg.commands;

import org.workcraft.dom.Container;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualStg;

public final class InsertInternalTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Insert internal transitions into selected arcs";
    }

    @Override
    public String getPopupName() {
        return "Insert internal transition";
    }

    @Override
    public VisualTransition createTransition(VisualStg stg, Container container) {
        return stg.createVisualSignalTransition(null,
                Signal.Type.INTERNAL, SignalTransition.Direction.TOGGLE, container);
    }

}
