package org.workcraft.plugins.punf.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.serialisation.SerialiserUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SpotChainTask implements Task<SpotChainOutput> {

    private final WorkspaceEntry we;
    private final String data;

    public SpotChainTask(WorkspaceEntry we, String data) {
        this.we = we;
        this.data = data;
    }

    @Override
    public Result<? extends SpotChainOutput> run(ProgressMonitor<? super SpotChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        WorkspaceEntry we = getWorkspaceEntry();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);

        try {
            // Convert SPOT assertion to Buechi automaton
            File spotFile = new File(directory, "assertion.spot");
            spotFile.deleteOnExit();
            try {
                FileUtils.dumpString(spotFile, getData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Ltl2tgbaTask ltl2tgbaTask = new Ltl2tgbaTask(spotFile, directory);
            Result<? extends Ltl2tgbaOutput> ltl2tgbaResult = manager.execute(
                    ltl2tgbaTask, "Converting SPOT assertion to B\u00FCchi automaton", subtaskMonitor);

            if (ltl2tgbaResult.getOutcome() != Result.Outcome.SUCCESS) {
                if (ltl2tgbaResult.getOutcome() == Result.Outcome.CANCEL) {
                    return new Result<>(Result.Outcome.CANCEL);
                }
                return new Result<>(Result.Outcome.FAILURE,
                        new SpotChainOutput(ltl2tgbaResult, null));
            }
            monitor.progressUpdate(0.1);

            // Export STG
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            File gFile = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgFormat.getInstance().getExtension());
            FileOutputStream gStream = new FileOutputStream(gFile);
            SerialiserUtils.writeModel(stg, gStream, SerialiserUtils.Style.STG, true);
            gStream.close();
            monitor.progressUpdate(0.3);

            // Generate unfolding
            File hoaFile = ltl2tgbaResult.getPayload().getOutputFile();
            PunfLtlxTask punfTask = new PunfLtlxTask(gFile, hoaFile, directory);
            Result<? extends PunfOutput> punfResult = manager.execute(punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Result.Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Result.Outcome.CANCEL) {
                    return new Result<>(Result.Outcome.CANCEL);
                }
                return new Result<>(Result.Outcome.FAILURE,
                        new SpotChainOutput(ltl2tgbaResult, punfResult));
            }
            monitor.progressUpdate(1.0);

            return new Result<>(Result.Outcome.SUCCESS,
                    new SpotChainOutput(ltl2tgbaResult, punfResult));
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    private String getData() {
        return data;
    }

}
