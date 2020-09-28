package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat_verification.tasks.RefinementTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class RefinementVerificationCommand extends AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    @Override
    public String getDisplayName() {
        return "Refinement [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public void run(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
        queueVerification(we, monitor);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, false);
        queueVerification(we, monitor);
        return monitor.waitForHandledResult();
    }

    private void queueVerification(WorkspaceEntry we, VerificationChainResultHandlingMonitor monitor) {
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        CircuitToStgConverter converter = new CircuitToStgConverter(circuit);
        Stg devStg = converter.getStg().getMathModel();
        File envFile = circuit.getMathModel().getEnvironmentFile();

        TaskManager manager = Framework.getInstance().getTaskManager();
        RefinementTask task = new RefinementTask(we, devStg, envFile, false, true);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
    }

}
