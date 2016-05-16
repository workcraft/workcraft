package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.SynthesisTool;
import org.workcraft.plugins.mpsat.MpsatSynthesisMode;
import org.workcraft.plugins.mpsat.MpsatSynthesisSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatSynthesisResultHandler;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;
/*
  To get Verilog from mpsat, just specify the output file with the extension *.v:
    mpsat -E -! file.bp.pnml file.cg.v
    mpsat -G -! file.bp.pnml file.gC.v
    mpsat -S -! file.bp.pnml file.stdC.v
    mpsat -T -f -p2 -cl -! file.bp.pnml file.mapped.v

  To feed a gate library, use the -d option:
    mpsat -T -f -p2 -cl -! -d gate_library.lib file.bp.pnml file.mapped.v
*/

public abstract class MpsatSynthesis extends SynthesisTool {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final MpsatSynthesisSettings settings = new MpsatSynthesisSettings("Logic synthesis", getSynthesisMode(), 0);
        final MpsatSynthesisChainTask task = new MpsatSynthesisChainTask(we, settings);

        final Framework framework = Framework.getInstance();
        framework.getTaskManager().queue(task, "MPSat logic synthesis", new MpsatSynthesisResultHandler(task));
    }

    public abstract MpsatSynthesisMode getSynthesisMode();

}
