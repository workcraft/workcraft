package org.workcraft.plugins.circuit.commands;

import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.tools.CycleAnalyserTool;
import org.workcraft.plugins.circuit.utils.CycleUtils;

import java.util.Collection;
import java.util.function.Function;

public class PathBreakerAutoDiscardTagCommand extends AbstractTagCommand {

    @Override
    Function<Circuit, Collection<Contact>> getFunction() {
        return circuit -> CycleUtils.tagPathBreakerAutoDiscard(circuit);
    }

    @Override
    String getMessage() {
        return "Auto-discarded path breaker pin";
    }

    @Override
    Class<? extends GraphEditorTool> getToolClass() {
        return CycleAnalyserTool.class;
    }

}
