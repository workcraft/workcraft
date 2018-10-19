package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.io.File;
import java.util.*;

public class MpsatConformationNwayTask implements Task<MpsatChainOutput> {

    private final MpsatParameters toolchainPreparationSettings = new MpsatParameters("Toolchain preparation of data",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final MpsatParameters toolchainCompletionSettings = new MpsatParameters("Toolchain completion",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final ArrayList<WorkspaceEntry> wes;

    public MpsatConformationNwayTask(ArrayList<WorkspaceEntry> wes) {
        this.wes = wes;
    }

    @Override
    public Result<? extends MpsatChainOutput> run(ProgressMonitor<? super MpsatChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();

        String prefix = FileUtils.getTempPrefix("-pcomp");
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        try {
            List<File> stgFiles = new ArrayList<>();
            List<Map<String, String>> substitutes = new ArrayList<>();
            for (WorkspaceEntry we: wes) {
                // Clone STG before converting its internal signals to dummies
                ModelEntry me = framework.cloneModel(we.getModelEntry());
                Stg stg = WorkspaceUtils.getAs(me, Stg.class);
                Map<String, String> dummy2InternalRefs = StgUtils.convertInternalSignalsToDummies(stg);
                substitutes.add(dummy2InternalRefs);

                // Generating .g for the model
                File stgFile = new File(directory, we.getTitle() + stgFileExtension);
                stgFiles.add(stgFile);

                Result<? extends ExportOutput> exportResult = StgUtils.exportStg(stg, stgFile, monitor);
                if (exportResult.getOutcome() != Outcome.SUCCESS) {
                    if (exportResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new MpsatChainOutput(exportResult, null, null, null, toolchainPreparationSettings));
                }
            }
            Result<MultiExportOutput> multiExportResult = new Result<>(new MultiSubExportOutput(stgFiles, substitutes));
            monitor.progressUpdate(0.30);

            // Generating .g for the whole system (model and environment)
            File sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
            File detailFile = new File(directory, StgUtils.DETAIL_FILE_PREFIX + StgUtils.XML_FILE_EXTENSION);
            PcompTask pcompTask = new PcompTask(stgFiles.toArray(new File[0]), sysStgFile, detailFile,
                    ConversionMode.OUTPUT, false, false, directory);

            Result<? extends PcompOutput> pcompResult = taskManager.execute(
                    pcompTask, "Running parallel composition [PComp]", new SubtaskMonitor<>(monitor));

            if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                if (pcompResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(multiExportResult, pcompResult, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Insert shadow transitions into the composed STG
            CompositionData compositionData = new CompositionData(detailFile);
            Stg modSysStg = StgUtils.loadStg(sysStgFile);
            Set<String> shadowTransitons = generateShadowTransitions(modSysStg, compositionData);
            File modSysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension);
            Result<? extends ExportOutput> exportResult = StgUtils.exportStg(modSysStg, modSysStgFile, monitor);

            // Generate unfolding
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(modSysStgFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = taskManager.execute(
                    punfTask, "Unfolding .g", new SubtaskMonitor<>(monitor));

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.60);

            // Check for conformation
            MpsatParameters conformationSettings = MpsatParameters.getConformationNwaySettings(shadowTransitons);
            MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, sysStgFile);
            Result<? extends MpsatOutput>  mpsatConformationResult = taskManager.execute(
                    mpsatConformationTask, "Running conformation check [MPSat]", new SubtaskMonitor<>(monitor));

            if (mpsatConformationResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatConformationResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings));
            }
            monitor.progressUpdate(0.80);

            MpsatOutputParser mpsatConformationParser = new MpsatOutputParser(mpsatConformationResult.getPayload());
            if (!mpsatConformationParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new MpsatChainOutput(multiExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings,
                                "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            unfoldingFile.delete();
            String message = "N-way conformation holds.";
            return new Result<>(Outcome.SUCCESS,
                    new MpsatChainOutput(multiExportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private Set<String> generateShadowTransitions(Stg compStg, CompositionData compositionData) {
        Set<String> result = new HashSet<>();
        for (String stgFileName : compositionData.getFileNames()) {
            ComponentData componentData = compositionData.getComponentData(stgFileName);
            Set<String> shadowTransitons = generateShadowTransitions(compStg, componentData);
            result.addAll(shadowTransitons);
        }
        return result;
    }

    private Set<String> generateShadowTransitions(Stg compStg, ComponentData componentData) {
        Set<String> result = new HashSet<>();
        File stgFile = new File(componentData.getFileName());
        Stg stg = StgUtils.loadStg(stgFile);
        Set<String> dstPlaceRefs = componentData.getDstPlaces();
        for (String signalRef : stg.getSignalReferences(Signal.Type.OUTPUT)) {
            for (SignalTransition signalTransition : compStg.getSignalTransitions(signalRef)) {
                Set<StgPlace> srcPlaces = new HashSet<>();
                for (StgPlace place : compStg.getPreset(signalTransition, StgPlace.class)) {
                    String dstPlaceRef = compStg.getNodeReference(place);
                    if (dstPlaceRefs.contains(dstPlaceRef)) {
                        srcPlaces.add(place);
                    }
                }
                if (!srcPlaces.isEmpty()) {
                    String signalName = signalTransition.getSignalName();
                    SignalTransition.Direction direction = signalTransition.getDirection();
                    Container container = Hierarchy.getNearestContainer(signalTransition);
                    SignalTransition shadowTransition = compStg.createSignalTransition(signalName, direction, container);
                    result.add(compStg.getNodeReference(shadowTransition));
                    for (StgPlace srcPlace : srcPlaces) {
                        compStg.makeExplicit(srcPlace);
                        try {
                            compStg.connect(srcPlace, shadowTransition);
                        } catch (InvalidConnectionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return result;
    }

}
