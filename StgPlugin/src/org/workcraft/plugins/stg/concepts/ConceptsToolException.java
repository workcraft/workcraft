package org.workcraft.plugins.stg.concepts;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgSettings;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.LogUtils;

@SuppressWarnings("serial")
public class ConceptsToolException extends Exception {

    private static final MainWindow mainWindow = Framework.getInstance().getMainWindow();
    private final Result<? extends ExternalProcessResult> result;

    public ConceptsToolException(Result<? extends ExternalProcessResult> result) {
        this.result = result;
    }

    public void handleConceptsError() {
        try {
            if (result.getOutcome() == Outcome.FAILED) {
                String errors = new String(result.getReturnValue().getErrors());
                System.out.println(LogUtils.PREFIX_STDERR + errors);
                if (errors.contains("<no location info>")) {
                    conceptsCodeNotFound();
                } else if (errors.contains("Could not find module")) {
                    conceptsNotInstalled();
                } else {
                    defaultError();
                }
            } else {
                String output = new String(result.getReturnValue().getOutput());
                if (!output.startsWith(".model out")) {
                    cannotTranslateConceptsError(output);
                }
            }
        } catch (NullPointerException e) {
            ghcNotFound();
        }
    }

    private void ghcNotFound() {
        JOptionPane.showMessageDialog(mainWindow, "runghc could not run, please install Haskell", "GHC not installed", JOptionPane.ERROR_MESSAGE);
    }

    private void conceptsCodeNotFound() {
        JOptionPane.showMessageDialog(mainWindow, "Concepts code could not be found. \n"
                + "Download it from https://github.com/tuura/concepts. \n"
                + "Ensure that the preferences menu points to the correct location of the concepts folder", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void conceptsNotInstalled() {
        String pkg = StgSettings.getConceptsFolderLocation();
        JOptionPane.showMessageDialog(mainWindow, "Concepts could not be run. \n"
                + "The " + pkg + " package needs to be installed via Cabal using the command \"cabal install " + pkg + "\"\n",
                "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void defaultError() {
        JOptionPane.showMessageDialog(mainWindow, "Concepts could not be translated", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void cannotTranslateConceptsError(String output) {

        System.out.println(LogUtils.PREFIX_STDERR + output);

        JOptionPane.showMessageDialog(mainWindow, "Concepts could not be translated."
                + "\nSee console window for error information", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }
}
