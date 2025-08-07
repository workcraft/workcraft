package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.Framework;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatSyntaxCheckTask;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class MpsatUtils {

    public static String getToolchainDescription(String title) {
        String result = "MPSat tool chain";
        if ((title != null) && !title.isEmpty()) {
            result += " (" + title + ")";
        }
        return result;
    }


    }

    public static void checkSyntax(WorkspaceEntry we, CodePanel codePanel, VerificationParameters verificationParameters) {
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);

        MpsatSyntaxCheckTask task = new MpsatSyntaxCheckTask(verificationParameters, directory);
        TaskManager manager = Framework.getInstance().getTaskManager();
        Result<? extends ExternalProcessOutput> result = manager.execute(task, "Checking REACH assertion syntax");

        if (result.isSuccess()) {
            String message = "Property is syntactically correct";
            codePanel.showInfoStatus(message);
            LogUtils.logInfo(message);
        }

        if (result.isFailure()) {
            BisonSyntaxUtils.processSyntaxError("Error: incorrect syntax of the expression: ",
                    result.getPayload(), codePanel);
        }
    }

}
