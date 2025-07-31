package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.interop.Format;
import org.workcraft.plugins.mpsat_verification.tasks.ConformationTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.File;

public class ConformationVerificationCommand
        extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, File> {

    @Override
    public String getDisplayName() {
        return "1-way conformation...";
    }

    @Override
    public Section getSection() {
        return AbstractRefinementVerificationCommand.SECTION;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        Format format = StgFormat.getInstance();
        JFileChooser fc = DialogUtils.createFileOpener("Select environment STG file", true, format);
        if (DialogUtils.showFileOpener(fc)) {
            File data = fc.getSelectedFile();
            queueTask(we, data);
        }
    }

    private VerificationChainResultHandlingMonitor queueTask(WorkspaceEntry we, File data) {
        TaskManager manager = Framework.getInstance().getTaskManager();
        ConformationTask task = new ConformationTask(we, data);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we);
        manager.queue(task, description, monitor);
        return monitor;
    }

    @Override
    public File deserialiseData(String data) {
        return Framework.getInstance().getFileByAbsoluteOrRelativePath(data);
    }

    @Override
    public Boolean execute(WorkspaceEntry we, File data) {
        VerificationChainResultHandlingMonitor monitor = queueTask(we, data);
        monitor.setInteractive(false);
        return monitor.waitForHandledResult();
    }

}
