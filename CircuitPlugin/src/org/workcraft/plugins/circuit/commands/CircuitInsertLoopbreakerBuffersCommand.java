/*
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All rights reserved.
 */

package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.utils.LoopUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class CircuitPathbreakerInsertBuffersCommand extends CircuitAbstractPathbreakerCommand {

    @Override
    public String getDisplayName() {
        return "Insert loop breaking buffers";
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        we.captureMemento();
        Collection<VisualFunctionComponent> loopbreakBuffers = LoopUtils.insertLoopBreakerBuffers(circuit);
        if (loopbreakBuffers.isEmpty()) {
            we.cancelMemento();
        } else {
            we.saveMemento();
        }
        return null;
    }

}
