package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.workspace.ModelEntry;

public class MutexProtocolEarlyTransformationCommand extends AbstractMutexProtocolTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Transform mutex to early protocol (selected or all)";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Transform to early protocol mutex";
    }

    @Override
    public Mutex.Protocol getMutexProtocol() {
        return Mutex.Protocol.EARLY;
    }

}
