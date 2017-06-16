package org.workcraft.plugins.cpog.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.util.MessageUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ScencoResultHandler extends DummyProgressMonitor<ScencoResult> {
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
    public void finished(Result<? extends ScencoResult> result, String description) {
        if (result.getOutcome() == Outcome.FINISHED) {
            String[] stdoutLines = result.getReturnValue().getStdout().split("\n");
            String resultDirectory = result.getReturnValue().getResultDirectory();
            solver.handleResult(stdoutLines, resultDirectory);

            // Import Verilog file into circuit
            if (solver.isVerilog()) {
                try {
                    byte[] verilogBytes = solver.getVerilog();
                    final ByteArrayInputStream in = new ByteArrayInputStream(verilogBytes);
                    final VerilogImporter verilogImporter = new VerilogImporter(false);
                    final Circuit circuit = verilogImporter.importCircuit(in);
                    final ModelEntry me = new ModelEntry(new CircuitDescriptor(), circuit);
                    final Path<String> path = we.getWorkspacePath();
                    final Framework framework = Framework.getInstance();
                    final MainWindow mainWindow = framework.getMainWindow();
                    final WorkspaceEntry weCircuit = framework.createWork(me, path);
                    final VisualModel visualModel = weCircuit.getModelEntry().getVisualModel();
                    if (visualModel instanceof VisualCircuit) {
                        final VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                        final String title = we.getModelEntry().getModel().getTitle();
                        visualCircuit.setTitle(title);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                mainWindow.getCurrentEditor().updatePropertyView();
                            }
                        });
                    }
                } catch (DeserialisationException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (result.getOutcome() == Outcome.FAILED) {
            final String errorMessage = getErrorMessage(result.getReturnValue());

            // In case of an internal error, activate automatically verbose mode
            if (errorMessage.equals(INTERNAL_ERROR_MSG)) {
                final String[] sentence = result.getReturnValue().getStdout().split("\n");
                for (int i = 0; i < sentence.length; i++) {
                    System.out.println(sentence[i]);
                }
            }

            //Removing temporary files
            final File dir = solver.getDirectory();
            FileUtils.deleteOnExitRecursively(dir);

            //Display the error
            MessageUtils.showError(errorMessage);
        }
    }

    // Get the error from the STDOUT of SCENCO
    private String getErrorMessage(ScencoResult scencoResult) {

        // SCENCO accessing error
        if (scencoResult.getStdout().equals(ScencoSolver.ACCESS_SCENCO_ERROR)) {
            return scencoResult.getError();
        }

        // SCENCO known error
        String[] sentence = scencoResult.getStdout().split("\n");
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
