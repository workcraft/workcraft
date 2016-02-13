package org.workcraft.plugins.cpog.tasks;

import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;

public class ScencoResultHandler extends DummyProgressMonitor<ScencoResult> {

    public static final String INTERNAL_ERROR_MSG = "Internal error. Contact developers at www.workcraft.org/";

    private ScencoExternalToolTask scenco;
    private ScencoSolver solver;

    public ScencoResultHandler(ScencoExternalToolTask scencoTask) {
        this.scenco = scencoTask;
        this.solver = scencoTask.getSolver();
    }

    @Override
    public void finished(Result<? extends ScencoResult> result, String description) {
        if (result.getOutcome() == Outcome.FINISHED) {
            String[] stdoutLines = result.getReturnValue().getStdout().split("\n");
            String resultDirectory = result.getReturnValue().getResultDirectory();
            solver.handleResult(stdoutLines, resultDirectory);
        } else if (result.getOutcome() == Outcome.FAILED) {
            String errorMessage = getErrorMessage(result.getReturnValue().getStdout());
            final Framework framework = Framework.getInstance();

            // In case of an internal error, activate automatically verbose mode
            if (errorMessage.equals(INTERNAL_ERROR_MSG)) {
                String[] sentence = result.getReturnValue().getStdout().split("\n");
                for (int i = 0; i < sentence.length; i++) {
                    System.out.println(sentence[i]);
                }
            }

            //Removing temporary files
            File dir = solver.getDirectory();
            FileUtils.deleteFile(dir, CommonDebugSettings.getKeepTemporaryFiles());

            //Display the error
            JOptionPane.showMessageDialog(framework.getMainWindow(), errorMessage, "SCENCO error", JOptionPane.ERROR_MESSAGE);

        }
    }

    // Get the error from the STDOUT of SCENCO
    private String getErrorMessage(String msg) {

        // SCENCO accessing error
        if (msg.equals(solver.ACCESS_SCENCO_ERROR)) {
            return solver.getScencoArguments().get(1);
        }

        // SCENCO known error
        String[] sentence = msg.split("\n");
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
