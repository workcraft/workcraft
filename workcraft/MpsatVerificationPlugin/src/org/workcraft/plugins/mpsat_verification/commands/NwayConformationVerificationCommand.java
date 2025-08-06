package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat_verification.gui.NwayConformationDialog;
import org.workcraft.plugins.mpsat_verification.tasks.AbstractChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.CompositionChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.NwayConformationTask;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.pcomp.utils.PcompUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NwayConformationVerificationCommand
        extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, List<WorkspaceEntry>> {

    @Override
    public String getDisplayName() {
        return "N-way conformation...";
    }

    @Override
    public Section getSection() {
        return AbstractRefinementVerificationCommand.SECTION;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return (we == null) || WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();
        MainWindow mainWindow = framework.getMainWindow();
        NwayConformationDialog dialog = new NwayConformationDialog(mainWindow);
        if (dialog.reveal()) {
            List<WorkspaceEntry> wes = dialog.getSourcePaths().stream()
                    .map(workspace::getWork)
                    .sorted((we1, we2) -> SortUtils.compareNatural(we1.getTitle(), we2.getTitle()))
                    .collect(Collectors.toList());

            queueTask(wes);
        }
    }

    private AbstractChainResultHandlingMonitor<VerificationChainOutput> queueTask(List<WorkspaceEntry> wes) {
        AbstractChainResultHandlingMonitor<VerificationChainOutput> monitor = getMonitor(wes);
        if (wes.size() < 2) {
            monitor.isFinished(Result.exception("At least two STGs are required for N-way conformation."));
            return monitor;
        }

        NwayConformationTask task = new NwayConformationTask(wes, getRenames());
        TaskManager manager = Framework.getInstance().getTaskManager();
        String titles = wes.stream().map(WorkspaceEntry::getTitle).collect(Collectors.joining(", "));
        String description = MpsatUtils.getToolchainDescription(titles);
        manager.queue(task, description, monitor);
        return monitor;
    }

    @Override
    public List<WorkspaceEntry> deserialiseData(String data) {
        return PcompUtils.deserialiseData(data);
    }

    @Override
    public Boolean execute(WorkspaceEntry we, List<WorkspaceEntry> wes) {
        AbstractChainResultHandlingMonitor<VerificationChainOutput> monitor = queueTask(wes);
        monitor.setInteractive(false);
        return monitor.waitForHandledResult();
    }

    public AbstractChainResultHandlingMonitor<VerificationChainOutput> getMonitor(List<WorkspaceEntry> wes) {
        return new CompositionChainResultHandlingMonitor(wes);
    }

    public Map<WorkspaceEntry, Map<String, String>> getRenames() {
        return Collections.emptyMap();
    }

}
