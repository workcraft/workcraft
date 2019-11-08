package org.workcraft.plugins.policy.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationOutputParser;
import org.workcraft.plugins.mpsat.tasks.VerificationTask;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.policy.VisualPolicy;
import org.workcraft.plugins.policy.tools.PolicyToPetriConverter;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class DeadlockFreenessTask implements Task<VerificationChainOutput> {

    private final WorkspaceEntry we;

    public DeadlockFreenessTask(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        final Framework framework = Framework.getInstance();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        VerificationParameters settings = new VerificationParameters("Deadlock freeness",
                VerificationMode.DEADLOCK, 0,
                MpsatVerificationSettings.getSolutionMode(), MpsatVerificationSettings.getSolutionCount());
        try {
            VisualPolicy policy = WorkspaceUtils.getAs(we, VisualPolicy.class);
            PolicyToPetriConverter converter = new PolicyToPetriConverter(policy);
            Petri model = (Petri) converter.getPetriNet().getMathModel();
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
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(exportResult, null, null, null, settings));
            }
            monitor.progressUpdate(0.20);

            File unfoldingFile = new File(directory, "unfolding" + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = framework.getTaskManager().execute(
                    punfTask, "Unfolding .g", mon);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(exportResult, null, punfResult, null, settings));
            }
            monitor.progressUpdate(0.70);

            VerificationTask verificationTask = new VerificationTask(settings.getMpsatArguments(directory),
                    unfoldingFile, directory, netFile);
            Result<? extends VerificationOutput> mpsatResult = framework.getTaskManager().execute(
                    verificationTask, "Running deadlock checking [MPSat]", mon);

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                String errorMessage = mpsatResult.getPayload().getErrorsHeadAndTail();
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(exportResult, null, punfResult, mpsatResult, settings, errorMessage));
            }
            monitor.progressUpdate(0.90);

            VerificationOutputParser mdp = new VerificationOutputParser(mpsatResult.getPayload());
            if (!mdp.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new VerificationChainOutput(exportResult, null, punfResult, mpsatResult, settings, "Policy net has a deadlock"));
            }
            monitor.progressUpdate(1.0);

            return new Result<>(Outcome.SUCCESS,
                    new VerificationChainOutput(exportResult, null, punfResult, mpsatResult, settings, "Policy net is deadlock-free"));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
