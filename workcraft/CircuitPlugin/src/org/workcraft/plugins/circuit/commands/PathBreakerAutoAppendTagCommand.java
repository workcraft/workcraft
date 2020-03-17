package org.workcraft.plugins.circuit.commands;

import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.tools.CycleAnalyserTool;
import org.workcraft.plugins.circuit.utils.CycleUtils;

import java.util.Collection;
import java.util.function.Function;

public class PathBreakerAutoAppendTagCommand extends AbstractTagCommand {

    @Override
    public Function<Circuit, Collection<Contact>> getFunction() {
        return circuit -> CycleUtils.tagPathBreakerAutoAppend(circuit);
    }

    @Override
    public String getMessage() {
        return "Auto-appended path breaker pin";
    }

    @Override
    public Class<? extends GraphEditorTool> getToolClass() {
        return CycleAnalyserTool.class;
    }

}
