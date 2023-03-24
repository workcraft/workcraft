package org.workcraft.plugins.circuit.commands;

import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.tools.InitialisationAnalyserTool;
import org.workcraft.plugins.circuit.utils.ResetUtils;

import java.util.Collection;
import java.util.function.Function;

public class ForcedInitAutoAppendTagCommand extends AbstractTagCommand {

    @Override
    public Function<Circuit, Collection<Contact>> getFunction() {
        return ResetUtils::tagForcedInitAutoAppend;
    }

    @Override
    public String getMessage() {
        return "Auto-appended forced init pin";
    }

    @Override
    public Class<? extends GraphEditorTool> getToolClass() {
        return InitialisationAnalyserTool.class;
    }

}
