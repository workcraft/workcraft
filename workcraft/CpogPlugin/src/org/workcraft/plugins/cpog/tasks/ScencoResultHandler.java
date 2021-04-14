package org.workcraft.plugins.cpog.tasks;

import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.tasks.BasicProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class ScencoResultHandler extends BasicProgressMonitor<ScencoOutput> {
    public static final String INTERNAL_ERROR_MSG = "Internal error. Contact developers at " + Info.getHomepage();

    private ScencoExternalToolTask scenco;
    private final ScencoSolver solver;
    private final WorkspaceEntry we;

    public ScencoResultHandler(ScencoExternalToolTask scencoTask) {
        this.scenco = scencoTask;
        this.solver = scencoTask.getSolver();
        this.we = scencoTask.getWorkspaceEntry();
    }

    @Override
    public void isFinished(Result<? extends ScencoOutput> result) {
        super.isFinished(result);
        if (result.isSuccess()) {
            String[] stdoutLines = result.getPayload().getStdout().split("\n");
            String resultDirectory = result.getPayload().getResultDirectory();
            solver.handleResult(stdoutLines, resultDirectory);

            // Import Verilog file into circuit
            if (solver.isVerilog()) {
                VerilogModule verilogModule = solver.getVerilogModule();
                VerilogImporter verilogImporter = new VerilogImporter();
                Circuit circuit = verilogImporter.createCircuit(verilogModule);
                ModelEntry me = new ModelEntry(new CircuitDescriptor(), circuit);
                Framework framework = Framework.getInstance();
                WorkspaceEntry weCircuit = framework.createWork(me, we.getFileName());
                VisualCircuit visualCircuit = WorkspaceUtils.getAs(weCircuit, VisualCircuit.class);
                visualCircuit.setTitle(we.getModelTitle());
                framework.updatePropertyView();
            }
        } else if (result.isFailure()) {
            final String errorMessage = getErrorMessage(result.getPayload());

            // In case of an internal error, activate automatically verbose mode
            if (INTERNAL_ERROR_MSG.equals(errorMessage)) {
                final String[] sentence = result.getPayload().getStdout().split("\n");
                for (int i = 0; i < sentence.length; i++) {
                    System.out.println(sentence[i]);
                }
            }

            //Removing temporary files
            final File dir = solver.getDirectory();
            FileUtils.deleteOnExitRecursively(dir);

            //Display the error
            DialogUtils.showError(errorMessage);
        }
    }

    // Get the error from the STDOUT of SCENCO
    private String getErrorMessage(ScencoOutput scencoOutput) {

        // SCENCO accessing error
        if (ScencoSolver.ACCESS_SCENCO_ERROR.equals(scencoOutput.getStdout())) {
            return scencoOutput.getError();
        }

        // SCENCO known error
        String[] sentence = scencoOutput.getStdout().split("\n");
        int i = 0;
        for (i = 0; i < sentence.length; i++) {
            if (sentence[i].contains(".error")) {
                return sentence[i + 1];
            }
        }

        // SCENCO undefined error
        return INTERNAL_ERROR_MSG;
    }

    // GETTER AND SETTER
    public ScencoExternalToolTask getScenco() {
        return scenco;
    }

    public void setScenco(ScencoExternalToolTask scenco) {
        this.scenco = scenco;
    }
}
