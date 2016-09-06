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
                } else if (errors.contains("Could not find module") || errors.contains("Failed to load interface")) {
                    conceptsNotInstalled();
                } else {
                    cannotTranslateConceptsError(errors);
                }
            } else {
                String output = new String(result.getReturnValue().getOutput());
                if (!output.startsWith(".model out")) {
                    if (output.contains("Error.")) {
                        System.out.println(LogUtils.PREFIX_STDERR + output);
                        if (output.contains("The following signals are not declared as input, output or internal")) {
                            signalTypeNotDeclared();
                        }
                        if (output.contains("The following signals have inconsistent inital states")) {
                            inconsistentStates();
                        }
                        if (output.contains("The following signals have undefined initial states")) {
                            undefinedStates();
                        }
                    } else {
                        cannotTranslateConceptsError(output);
                    }
                }
            }
        } catch (NullPointerException e) {
            ghcNotFound();
        }
    }

    private void ghcNotFound() {
        JOptionPane.showMessageDialog(mainWindow, "Stack could not run, please download and install Stack from:\n"
                + "https://www.haskell.org/downloads#stack", "GHC not installed", JOptionPane.ERROR_MESSAGE);
    }

    private void conceptsCodeNotFound() {
        JOptionPane.showMessageDialog(mainWindow, "Concepts code could not be found. \n"
                + "Download it from https://github.com/tuura/concepts. \n"
                + "Ensure that the preferences menu points to the correct location of the concepts folder", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void conceptsNotInstalled() {
        String pkg = StgSettings.getConceptsFolderLocation();
        JOptionPane.showMessageDialog(mainWindow, "Concepts could not be run. \n"
                + "The " + pkg + " package needs to be installed via stack. To do this: \n"
                + "1. Make sure stack is installed (https://www.haskell.org/downloads#stack).\n"
                + "2. In the terminal navigate to the concepts folder, found in \"" + pkg + "\" within the Workcraft directory.\n"
                + "3. Run the command \"stack setup\".\n"
                + "4. Run the command \"stack build\".\n"
                + "Then, rerun the concepts translation.",
                "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void cannotTranslateConceptsError(String output) {

        System.out.println(LogUtils.PREFIX_STDERR + output);

        JOptionPane.showMessageDialog(mainWindow, "Concepts could not be translated."
                + "\nSee console window for error information", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void signalTypeNotDeclared() {
        JOptionPane.showMessageDialog(mainWindow, ""
                + "One or more signals have not had their type declared. \n"
                + "A list of these can be found in the console window.\n"
                + "This can be done by including one of the concepts: \"input\", \"output\" or \"internal\""
                + "\nalong with the list of signals of those types."
                + "\nE.g input [a, b] <> output [c] <> internal [x]", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void inconsistentStates() {
        JOptionPane.showMessageDialog(mainWindow, ""
                + "One or more signals has inconsistent initial states.\n"
                + "A list of these signals can be found in the console window.\n"
                + "This occurs when a signal has their initial state declared both high (1) and low (0).",
                "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void undefinedStates() {
        JOptionPane.showMessageDialog(mainWindow, ""
                + "One or more signals has undefined initial states.\n"
                + "A list of these signals can be found in the console window.\n"
                + "These signals have no initial state declared. Initial states can be set using any of the following concepts:\n"
                + "\"initialise a False <> initialise b True <> initialise0 [x, y, z] <> initialise1 [p, q]",
                "Concept translation failed", JOptionPane.ERROR_MESSAGE);
    }
}
