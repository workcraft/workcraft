package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.interop.Format;
import org.workcraft.plugins.mpsat_verification.tasks.RefinementTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.File;

public class RefinementVerificationCommand
        extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, File> {

    @Override
    public String getDisplayName() {
        return "Refinement...";
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
        JFileChooser fc = DialogUtils.createFileOpener("Select specification STG file", true, format);
        if (DialogUtils.showFileOpener(fc)) {
            File data = fc.getSelectedFile();
            queueTask(we, data);
        }
    }

    private VerificationChainResultHandlingMonitor queueTask(WorkspaceEntry we, File data) {
        // Clone implementation STG as its internal signals will need to be converted to dummies
        ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
        Stg stg = WorkspaceUtils.getAs(me, Stg.class);

        TaskManager manager = Framework.getInstance().getTaskManager();
        RefinementTask task = new RefinementTask(we, stg, data, getAllowConcurrencyReduction(), false, true);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we);
        manager.queue(task, description, monitor);
        return monitor;
    }

    public boolean getAllowConcurrencyReduction() {
        return false;
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
