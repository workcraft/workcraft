package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.workspace.WorkspaceEntry;

class MpsatDeadlockFreenessOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatDeadlockFreenessOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(we, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public String getMessage(boolean isSatisfiable) {
        return "The system " + (isSatisfiable ? "has a deadlock" : "is deadlock-free");
    }

    @Override
    public String extendMessage(String message) {
        return "<html><br>&#160;" + message + " after the following trace(s):<br><br></html>";
    }

}
