package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class NwayConformationTask implements Task<VerificationChainOutput> {

    private static final String STG_FILE_EXTENSION = StgFormat.getInstance().getExtension();
    private static final String COMPOSITION_SHADOW_STG_FILE_NAME = "composition-shadow" + STG_FILE_EXTENSION;

    private final List<WorkspaceEntry> wes;

    public NwayConformationTask(List<WorkspaceEntry> wes) {
        this.wes = wes;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Result<? extends VerificationChainOutput> result = checkTrivialCases();
        if (result == null) {
            File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix("-nway_conformation"));
            Chain<VerificationChainOutput> chain = new Chain<>(this::init, monitor);
            chain.andOnSuccess(payload -> exportInterfaces(payload, monitor, directory), 0.1);
            chain.andOnSuccess(payload -> composeInterfaces(payload, monitor, directory), 0.2);
            chain.andOnSuccess(payload -> exportComposition(payload, monitor, directory), 0.3);
            chain.andOnSuccess(payload -> unfoldComposition(payload, monitor, directory), 0.5);
            chain.andOnSuccess(payload -> verifyProperty(payload, monitor, directory), 1.0);
            chain.andThen(() -> FileUtils.deleteOnExitRecursively(directory));
            result = chain.process();
        }
        return result;
    }

    private Result<? extends VerificationChainOutput> checkTrivialCases() {
        for (WorkspaceEntry we : wes) {
            if (!WorkspaceUtils.isApplicable(we, StgModel.class)) {
                return Result.exception("Incorrect model type for " + we.getTitle());
            }
        }
        return null;
    }

    private Result<? extends VerificationChainOutput> init() {
        VerificationParameters verificationParameters = ReachUtils.getToolchainPreparationParameters();
        return Result.success(new VerificationChainOutput().applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> exportInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        ExtendedExportOutput extendedExportOutput = new ExtendedExportOutput();
        for (WorkspaceEntry we : wes) {
            // Clone component STG as its internal signals will be converted to dummies
            ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
            Stg stg = WorkspaceUtils.getAs(me, Stg.class);

            // Export component STG (convert internal signals to dummies and keep track of renaming)
            @SuppressWarnings("PMD.PrematureDeclaration")
            Map<String, String> substitutions = StgUtils.convertInternalSignalsToDummies(stg);
            File stgFile = new File(directory, we.getTitle() + STG_FILE_EXTENSION);
            Result<? extends ExportOutput> exportResult = StgUtils.exportStg(stg, stgFile, monitor);
            if (!exportResult.isSuccess()) {
                return new Result<>(exportResult.getOutcome(), payload);
            }
            extendedExportOutput.add(stgFile, substitutions);
        }
        Result<ExtendedExportOutput> extendedExportResult = Result.success(extendedExportOutput);
        return Result.success(payload.applyExportResult(extendedExportResult));
    }

    private Result<? extends VerificationChainOutput> composeInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        List<File> componentStgFiles = wes.stream()
                .map(we -> getComponentStgFile(directory, we))
                .collect(Collectors.toList());

        PcompParameters pcompParameters = new PcompParameters(
                PcompParameters.SharedSignalMode.OUTPUT, false, false);

        // Note: the order of STG files is important, as it ts used in the analysis of violation traces
        PcompTask task = new PcompTask(componentStgFiles, pcompParameters, directory);

        Result<? extends PcompOutput> pcompResult = Framework.getInstance().getTaskManager().execute(
                task, "Running parallel composition [PComp]", new SubtaskMonitor<>(monitor));

        return new Result<>(pcompResult.getOutcome(), payload.applyPcompResult(pcompResult));
    }

    private Result<? extends VerificationChainOutput> exportComposition(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        PcompOutput pcompOutput = payload.getPcompResult().getPayload();
        CompositionData compositionData;
        try {
            compositionData = new CompositionData(pcompOutput.getDetailFile());
        } catch (FileNotFoundException e) {
            return Result.exception(e);
        }

        // Apply substitutions to the composition data of the STG components
        ExportOutput exportOutput = payload.getExportResult().getPayload();
        CompositionUtils.applyExportSubstitutions(compositionData, exportOutput);

        // Insert shadow transitions into the composition STG for device outputs and internal signals
        Stg compositionStg = StgUtils.importStg(pcompOutput.getOutputFile());
        CompositionTransformer transformer = new CompositionTransformer(compositionStg, compositionData);
        Set<SignalTransition> shadowTransitions = new HashSet<>();
        for (WorkspaceEntry we : wes) {
            StgModel componentStg = WorkspaceUtils.getAs(we, StgModel.class);
            Set<String> componentOutputSignals = componentStg.getSignalReferences(Signal.Type.OUTPUT);
            File componentStgFile = getComponentStgFile(directory, we);
            shadowTransitions.addAll(transformer.insetShadowTransitions(componentOutputSignals, componentStgFile));
        }

        // Fill verification parameters with the inserted shadow transitions
        Collection<String> shadowTransitionRefs = ReferenceHelper.getReferenceList(compositionStg, shadowTransitions);
        VerificationParameters verificationParameters = ReachUtils.getConformationParameters(shadowTransitionRefs);

        File shadowCompositionStgFile = new File(directory, COMPOSITION_SHADOW_STG_FILE_NAME);
        Result<? extends ExportOutput> exportResult = StgUtils.exportStg(compositionStg, shadowCompositionStgFile, monitor);
        CompositionExportOutput compositionExportOutput = new CompositionExportOutput(shadowCompositionStgFile, compositionData);

        return new Result<>(exportResult.getOutcome(), payload
                .applyExportResult(Result.success(compositionExportOutput))
                .applyVerificationParameters(verificationParameters));
    }

    private File getComponentStgFile(File directory, WorkspaceEntry we) {
        return new File(directory, we.getTitle() + STG_FILE_EXTENSION);
    }

    private Result<? extends VerificationChainOutput> unfoldComposition(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File stgFile = new File(directory, COMPOSITION_SHADOW_STG_FILE_NAME);
        PunfTask punfTask = new PunfTask(stgFile, directory);
        Result<? extends PunfOutput> punfResult = Framework.getInstance().getTaskManager().execute(
                punfTask, "Unfolding .g", new SubtaskMonitor<>(monitor));

        return new Result<>(punfResult.getOutcome(), payload.applyPunfResult(punfResult));
    }

    private Result<? extends VerificationChainOutput> verifyProperty(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File unfoldingFile = payload.getPunfResult().getPayload().getOutputFile();
        // Store composition STG  -- redundant !!!
        File compositionStgFile = new File(directory, COMPOSITION_SHADOW_STG_FILE_NAME);
        VerificationParameters verificationParameters = payload.getVerificationParameters();
        MpsatTask mpsatTask = new MpsatTask(unfoldingFile, compositionStgFile, verificationParameters, directory);
        Result<? extends MpsatOutput>  mpsatResult = Framework.getInstance().getTaskManager().execute(
                mpsatTask, "Running refinement check [MPSat]", new SubtaskMonitor<>(monitor));

        if (mpsatResult.isSuccess()) {
            payload = payload.applyMessage(mpsatResult.getPayload().hasSolutions()
                    ? "This model does not conform to the environment."
                    : "N-way conformation holds.");
        }

        return new Result<>(mpsatResult.getOutcome(), payload.applyMpsatResult(mpsatResult));
    }

}
