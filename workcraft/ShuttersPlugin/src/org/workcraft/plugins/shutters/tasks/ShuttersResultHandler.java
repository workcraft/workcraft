package org.workcraft.plugins.shutters.tasks;

import org.workcraft.Framework;
import org.workcraft.tasks.BasicProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.utils.FileUtils;

import javax.swing.*;
import java.io.File;

public class ShuttersResultHandler extends BasicProgressMonitor<ShuttersOutput>  {

    private final File tmpDir;

    public ShuttersResultHandler(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    @Override
    public void isFinished(Result<? extends ShuttersOutput> result) {
        super.isFinished(result);
        if (result.isSuccess()) {
            System.out.println(result.getPayload().getStdout());
        } else if (result.isFailure()) {
            FileUtils.deleteOnExitRecursively(tmpDir);
            String errorMessage = result.getPayload().getError();
            Framework framework = Framework.getInstance();
            JOptionPane.showMessageDialog(framework.getMainWindow(), errorMessage, "Shutters error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
