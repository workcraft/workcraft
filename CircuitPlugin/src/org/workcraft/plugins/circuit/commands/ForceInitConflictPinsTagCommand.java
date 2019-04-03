package org.workcraft.plugins.circuit.commands;

import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.tools.InitialisationAnalyserTool;
import org.workcraft.plugins.circuit.utils.ResetUtils;

import java.util.Collection;
import java.util.function.Function;

public class ForceInitConflictPinsTagCommand extends AbstractTagCommand {

    @Override
    Function<Circuit, Collection<Contact>> getFunction() {
        return circuit -> ResetUtils.tagForceInitConflictPins(circuit);
    }

    @Override
    String getMessage() {
        return "Force init conflicting state pin";
    }

    @Override
    Class<? extends GraphEditorTool> getToolClass() {
        return InitialisationAnalyserTool.class;
    }

}
