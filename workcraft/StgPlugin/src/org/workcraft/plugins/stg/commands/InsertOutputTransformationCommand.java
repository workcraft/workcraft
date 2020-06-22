package org.workcraft.plugins.stg.commands;

import org.workcraft.dom.Container;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.references.StgNameManager;

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
    public VisualTransition createTransition(VisualStg stg, Container container) {
        return stg.createVisualSignalTransition(StgNameManager.OUTPUT_SIGNAL_PREFIX,
                Signal.Type.OUTPUT, SignalTransition.Direction.TOGGLE, container);
    }

}
