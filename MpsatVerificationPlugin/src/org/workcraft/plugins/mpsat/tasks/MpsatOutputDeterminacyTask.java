package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.utils.TransformUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.io.File;
import java.util.*;

public class MpsatOutputDeterminacyTask implements Task<MpsatChainOutput> {

    private final WorkspaceEntry we;

    public MpsatOutputDeterminacyTask(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public Result<? extends MpsatChainOutput> run(ProgressMonitor<? super MpsatChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();

        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        MpsatParameters preparationSettings = new MpsatParameters("Toolchain preparation of data",
                MpsatMode.UNDEFINED, 0, null, 0);
        try {
            // Clone STG before converting its internal signals to outputs
            ModelEntry me = framework.cloneModel(we.getModelEntry());
            Stg stg = WorkspaceUtils.getAs(me, Stg.class);
            StgUtils.convertInternalSignalsToOutputs(stg);

            // Structural check for vacuously held output-determinacy, i.e. there are no dummies
            // and there are no choices between transitions of the same signal.
            if (isVacuouslyOutputDeterminate(stg)) {
                MpsatParameters vacuousSettings = new MpsatParameters("Output determinacy (vacuously)",
                        MpsatMode.UNDEFINED, 0, null, 0);
                return new Result<>(Outcome.SUCCESS,
                        new MpsatChainOutput(null, null, null, null,
                                vacuousSettings, "Output determinacy vacuously holds."));
            }
            monitor.progressUpdate(0.20);

            // Generating two copies of .g file for the model (dev and env)
            File devStgFile = new File(directory, StgUtils.DEVICE_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(stg, devStgFile, monitor);
            if (devExportResult.getOutcome() != Outcome.SUCCESS) {
                if (devExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(devExportResult, null, null, null, preparationSettings));
            }

            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(stg, envStgFile, monitor);
            if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                if (envExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(envExportResult, null, null, null, preparationSettings));
            }

            List<File> stgFiles = Arrays.asList(devStgFile, envStgFile);
            Result<MultiExportOutput> multiExportResult = new Result<>(new MultiExportOutput(stgFiles));
            monitor.progressUpdate(0.30);

            // Generating .g for the whole system (model and environment)
            File sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
            File detailFile = new File(directory, StgUtils.DETAIL_FILE_PREFIX + StgUtils.XML_FILE_EXTENSION);
            PcompTask pcompTask = new PcompTask(stgFiles.toArray(new File[0]), sysStgFile, detailFile,
                    ConversionMode.OUTPUT, true, false, directory);

            Result<? extends PcompOutput> pcompResult = taskManager.execute(
                    pcompTask, "Running parallel composition [PComp]", new SubtaskMonitor<>(monitor));

            if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                if (pcompResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(multiExportResult, pcompResult, null, null, preparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Insert shadow transitions into the composed STG
            CompositionData compositionData = new CompositionData(detailFile);
            ComponentData devComponentData = compositionData.getComponentData(devStgFile);
            Stg modSysStg = StgUtils.loadStg(sysStgFile);
            Set<String> devShadowTransitions = TransformUtils.generateShadows(modSysStg, devComponentData);
            File modSysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension);
            Result<? extends ExportOutput> modSysExportResult = StgUtils.exportStg(modSysStg, modSysStgFile, monitor);

            // Generate unfolding
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(modSysStgFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = taskManager.execute(
                    punfTask, "Unfolding .g", new SubtaskMonitor<>(monitor));

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(modSysExportResult, pcompResult, punfResult, null, preparationSettings));
            }
            monitor.progressUpdate(0.60);

            // Check for output determinacy
            MpsatParameters mpsatSettings = MpsatParameters.getOutputDeterminacySettings(devShadowTransitions);
            MpsatTask mpsatTask = new MpsatTask(mpsatSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, sysStgFile);
            Result<? extends MpsatOutput>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running output determinacy check [MPSat]", new SubtaskMonitor<>(monitor));

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(modSysExportResult, pcompResult, punfResult, mpsatResult, mpsatSettings));
            }
            monitor.progressUpdate(0.80);

            MpsatOutputParser mpsatParser = new MpsatOutputParser(mpsatResult.getPayload());
            if (!mpsatParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new MpsatChainOutput(multiExportResult, pcompResult, punfResult, mpsatResult, mpsatSettings,
                                "This model does is not output determinate."));
            }
            monitor.progressUpdate(1.0);

            // Success
            return new Result<>(Outcome.SUCCESS,
                    new MpsatChainOutput(multiExportResult, pcompResult, punfResult, mpsatResult,
                            mpsatSettings, "Output determinacy holds."));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private boolean isVacuouslyOutputDeterminate(Stg stg) {
        if (!stg.getDummyTransitions().isEmpty()) {
            return false;
        }
        for (String signal : stg.getSignalReferences()) {
            HashSet<MathNode> places = new HashSet<>();
            for (SignalTransition t : stg.getSignalTransitions(signal)) {
                Set<MathNode> preset = stg.getPreset(t);
                if (!Collections.disjoint(places, preset)) {
                    return false;
                }
                places.addAll(preset);
            }
        }
        return true;
    }

}
