package org.workcraft.plugins.policy.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.policy.tools.PolicyToPetriConverter;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.ExportUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CheckDeadlockTask extends MpsatChainTask {

    public CheckDeadlockTask(WorkspaceEntry we) {
        super(we, new MpsatParameters("Deadlock freeness", MpsatMode.DEADLOCK, 0,
                MpsatSettings.getSolutionMode(), MpsatSettings.getSolutionCount()));
    }

    @Override
    public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
        final Framework framework = Framework.getInstance();
        WorkspaceEntry we = getWorkspaceEntry();
        MpsatParameters settings = getSettings();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            VisualPolicyNet policy = WorkspaceUtils.getAs(we, VisualPolicyNet.class);
            PolicyToPetriConverter converter = new PolicyToPetriConverter(policy);
            PetriNet model = (PetriNet) converter.getPetriNet().getMathModel();
            StgFormat format = StgFormat.getInstance();
            Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), model, format);
            if (exporter == null) {
                throw new NoExporterException(model, format);
            }
            monitor.progressUpdate(0.10);

            File netFile = new File(directory, "net" + format.getExtension());
            ExportTask exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(
                    exportTask, "Exporting .g", mon);

            if (exportResult.getOutcome() != Outcome.SUCCESS) {
                if (exportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, null, null, null, settings));
            }
            monitor.progressUpdate(0.20);

            File unfoldingFile = new File(directory, "unfolding" + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            Result<? extends ExternalProcessOutput> punfResult = framework.getTaskManager().execute(
                    punfTask, "Unfolding .g", mon);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, null, punfResult, null, settings));
            }
            monitor.progressUpdate(0.70);

            MpsatTask mpsatTask = new MpsatTask(settings.getMpsatArguments(directory),
                    unfoldingFile, directory);
            Result<? extends ExternalProcessOutput> mpsatResult = framework.getTaskManager().execute(
                    mpsatTask, "Running deadlock checking [MPSat]", mon);

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                String errorMessage = mpsatResult.getPayload().getErrorsHeadAndTail();
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, null, punfResult, mpsatResult, settings, errorMessage));
            }
            monitor.progressUpdate(0.90);

            MpsatResultParser mdp = new MpsatResultParser(mpsatResult.getPayload());
            if (!mdp.getSolutions().isEmpty()) {
                return new Result<MpsatChainResult>(Outcome.SUCCESS,
                        new MpsatChainResult(exportResult, null, punfResult, mpsatResult, settings, "Policy net has a deadlock"));
            }
            monitor.progressUpdate(1.0);

            return new Result<MpsatChainResult>(Outcome.SUCCESS,
                    new MpsatChainResult(exportResult, null, punfResult, mpsatResult, settings, "Policy net is deadlock-free"));

        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
