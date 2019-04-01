package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.workspace.WorkspaceEntry;

class RedundancyOutputHandler extends ReachabilityOutputHandler {

    RedundancyOutputHandler(WorkspaceEntry we, VerificationOutput mpsatOutput, VerificationParameters settings) {
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
