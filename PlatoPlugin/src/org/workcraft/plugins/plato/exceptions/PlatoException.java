package org.workcraft.plugins.plato.exceptions;

import org.workcraft.plugins.plato.PlatoSettings;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;

@SuppressWarnings("serial")
public class PlatoException extends Exception {

    private final Result<? extends ExternalProcessOutput> result;

    public PlatoException(Result<? extends ExternalProcessOutput> result) {
        this.result = result;
    }

    public void handleConceptsError() {
        try {
            if (result.getOutcome() == Outcome.FAILURE) {
                String errors = result.getPayload().getStderrString();
                LogUtils.logStderr(errors);
                if (errors.contains("<no location info>")) {
                    conceptsCodeNotFound();
                } else if (errors.contains("Could not find module") || errors.contains("Failed to load interface")) {
                    conceptsNotInstalled();
                } else {
                    cannotTranslateConceptsError(errors);
                }
            } else {
                String stdout = result.getPayload().getStdoutString();
                if (!stdout.startsWith(".model out")) {
                    if (stdout.contains("Error.")) {
                        LogUtils.logStderr(stdout);
                        if (stdout.contains("The following signals are not declared as input, output or internal")) {
                            signalTypeNotDeclared();
                        }
                        if (stdout.contains("The following signals have inconsistent inital states")) {
                            inconsistentStates();
                        }
                        if (stdout.contains("The following signals have undefined initial states")) {
                            undefinedStates();
                        }
                    } else {
                        cannotTranslateConceptsError(stdout);
                    }
                }
            }
        } catch (NullPointerException e) {
            ghcNotFound();
        }
    }

    private void ghcNotFound() {
        DialogUtils.showError(
                "Stack could not run, please follow the instructions to install Stack from:\n"
                + "https://docs.haskellstack.org/en/stable/install_and_upgrade/",
                "GHC not installed");
    }

    private void conceptsCodeNotFound() {
        DialogUtils.showError(
                "Concepts code could not be found. \n"
                + "Download it from and follow the instructions to build it from https://github.com/tuura/concepts.\n"
                + "Ensure that the preferences menu points to the correct location of the concepts folder",
                "Concept translation failed");
    }

    private void conceptsNotInstalled() {
        String pkg = PlatoSettings.getPlatoFolderLocation();
        DialogUtils.showError(
                "Concepts could not be run. \n"
                + "The " + pkg + " package needs to be installed via stack. To do this: \n"
                + "1. Make sure stack is installed (https://docs.haskellstack.org/en/stable/install_and_upgrade/).\n"
                + "2. In the terminal navigate to the concepts folder, found in \"" + pkg + "\" within the Workcraft directory.\n"
                + "3. Run the command \"stack setup --no-system-ghc\".\n"
                + "4. Run the command \"stack build\".\n"
                + "Then, rerun the concepts translation.",
                "Concept translation failed");
    }

    private void cannotTranslateConceptsError(String output) {
        LogUtils.logStderr(output);
        DialogUtils.showError(
                "Concepts could not be translated."
                + "\nSee Problems tab for details",
                "Concept translation failed");
    }

    private void signalTypeNotDeclared() {
        DialogUtils.showError(
                "One or more signals have not had their type declared. \n"
                + "A list of these can be found in the console window.\n"
                + "This can be done by including one of the concepts: \"input\", \"output\" or \"internal\""
                + "\nalong with the list of signals of those types."
                + "\nE.g input [a, b] <> output [c] <> internal [x]",
                "Concept translation failed");
    }

    private void inconsistentStates() {
        DialogUtils.showError(
                "One or more signals has inconsistent initial states.\n"
                + "A list of these signals can be found in the console window.\n"
                + "This occurs when a signal has their initial state declared both high (1) and low (0).",
                "Concept translation failed");
    }

    private void undefinedStates() {
        DialogUtils.showError(
                "One or more signals has undefined initial states.\n"
                + "A list of these signals can be found in the console window.\n"
                + "These signals have no initial state declared. Initial states can be set using any of the following concepts:\n"
                + "\"initialise a False <> initialise b True <> initialise0 [x, y, z] <> initialise1 [p, q]",
                "Concept translation failed");
    }

}
