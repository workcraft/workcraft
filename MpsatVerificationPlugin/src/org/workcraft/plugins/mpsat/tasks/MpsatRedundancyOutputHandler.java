package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.workspace.WorkspaceEntry;

class MpsatRedundancyOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatRedundancyOutputHandler(WorkspaceEntry we, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(we, mpsatOutput, settings);
    }

    @Override
    public String getMessage(boolean isSatisfiable) {
        return "The selected places are " + (isSatisfiable ? "essential" : "redundant");
    }

    public String extendMessage(String message) {
        String traceInfo = "&#160;Trace(s) leading to the witness state(s):<br><br>";
        return "<html><br>&#160;" + message + "<br><br>" + traceInfo + "</html>";
    }

}
