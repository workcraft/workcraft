package org.workcraft.plugins.stg.commands;

import org.workcraft.dom.Container;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.references.StgNameManager;

public final class InsertInputTransformationCommand extends AbstractInsertTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Insert input transitions into selected arcs";
    }

    @Override
    public String getPopupName() {
        return "Insert input transition";
    }

    @Override
    public VisualTransition createTransition(VisualStg stg, Container container) {
        return stg.createVisualSignalTransition(StgNameManager.INPUT_SIGNAL_PREFIX,
                Signal.Type.INPUT, SignalTransition.Direction.TOGGLE, container);
    }

}
