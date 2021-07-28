package org.workcraft.plugins.circuit.commands;

import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.tools.CycleAnalyserTool;
import org.workcraft.plugins.circuit.utils.CycleUtils;

import java.util.Collection;
import java.util.function.Function;

public class PathBreakerSelfloopPinsTagCommand extends AbstractTagCommand {

    @Override
    public Function<Circuit, Collection<Contact>> getFunction() {
        return CycleUtils::tagPathBreakerSelfloopPins;
    }

    @Override
    public String getMessage() {
        return "Path breaker self-loop pin";
    }

    @Override
    public Class<? extends GraphEditorTool> getToolClass() {
        return CycleAnalyserTool.class;
    }

}
