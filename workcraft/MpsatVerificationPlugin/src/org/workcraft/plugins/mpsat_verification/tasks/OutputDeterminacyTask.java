package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.VerificationMode;
import org.workcraft.plugins.mpsat_verification.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.mpsat_verification.utils.TransformUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.types.Pair;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class OutputDeterminacyTask implements Task<VerificationChainOutput> {

    private static final String REPLACEMENT_SHADOW_TRANSITIONS =
            "/* insert set of names of shadow transitions here */"; // For example: "x+/1", "x-", "y+", "y-/1"

    private static final String REACH_OUTPUT_DETERMINACY =
            "// Check whether an STG is output-determinate.\n" +
            "// The enabled-via-dummies semantics is assumed for @.\n" +
            "// Configurations with maximal dummies are assumed to be allowed.\n" +
            "let\n" +
            "    // Set of phantom output transition names in the whole composed STG.\n" +
            "    SHADOW_OUTPUT_TRANSITIONS_NAMES = {" + REPLACEMENT_SHADOW_TRANSITIONS + "\"\"} \\ {\"\"},\n" +
            "    SHADOW_OUTPUT_TRANSITIONS = gather n in SHADOW_OUTPUT_TRANSITIONS_NAMES { T n }\n" +
            "{\n" +
            "    // Optimisation: make sure phantom events are not in the configuration.\n" +
            "    forall e in ev SHADOW_OUTPUT_TRANSITIONS \\ CUTOFFS { ~$e }\n" +
            "    &\n" +
            "    // Check if some output signal in the composition is enabled due to phantom transitions only;\n" +
            "    // this would mean that in the original STG there are two executions with the same visible\n" +
            "    // traces, leading to two markings M1 and M2 such that M1 enables some output signal o\n" +
            "    // but M2 does not enable o, so the output-determinacy is violated.\n" +
            "    exists o in OUTPUTS {\n" +
            "        let tran_o = tran o {\n" +
            "            exists t in tran_o * SHADOW_OUTPUT_TRANSITIONS {\n" +
            "                forall p in pre t { $p }\n" +
            "            }\n" +
            "            &\n" +
            "            forall tt in tran_o \\ SHADOW_OUTPUT_TRANSITIONS { ~@tt }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private final WorkspaceEntry we;

    public OutputDeterminacyTask(WorkspaceEntry we) {
        this.we = we;
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();

        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
        try {
            // Clone STG before converting its internal signals to outputs
            ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
            Stg stg = WorkspaceUtils.getAs(me, Stg.class);
            StgUtils.convertInternalSignalsToOutputs(stg);

            // Structural check for vacuously held output-determinacy, i.e. there are no dummies
            // and there are no choices between transitions of the same signal.
            if (isVacuouslyOutputDeterminate(stg)) {
                VerificationParameters vacuousParameters = new VerificationParameters("Output determinacy (vacuously)",
                        VerificationMode.UNDEFINED, 0, null, 0);
                return new Result<>(Outcome.SUCCESS,
                        new VerificationChainOutput(null, null, null, null,
                                vacuousParameters, "Output determinacy vacuously holds."));
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
                        new VerificationChainOutput(devExportResult, null, null, null, preparationParameters));
            }

            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(stg, envStgFile, monitor);
            if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                if (envExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(envExportResult, null, null, null, preparationParameters));
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
                        new VerificationChainOutput(multiExportResult, pcompResult, null, null, preparationParameters));
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
                        new VerificationChainOutput(modSysExportResult, pcompResult, punfResult, null, preparationParameters));
            }
            monitor.progressUpdate(0.60);

            // Check for output determinacy
            VerificationParameters verificationParameters = getVerificationParameters(devShadowTransitions);
            MpsatTask mpsatTask = new MpsatTask(unfoldingFile, sysStgFile, verificationParameters, directory);
            Result<? extends MpsatOutput>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running output determinacy check [MPSat]", new SubtaskMonitor<>(monitor));

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(modSysExportResult, pcompResult, punfResult, mpsatResult, verificationParameters));
            }
            monitor.progressUpdate(0.80);

            String mpsatStdout = mpsatResult.getPayload().getStdoutString();
            MpsatOutputParser mpsatParser = new MpsatOutputParser(mpsatStdout);
            if (!mpsatParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new VerificationChainOutput(multiExportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                                "This model is not output determinate."));
            }
            monitor.progressUpdate(1.0);

            // Success
            return new Result<>(Outcome.SUCCESS,
                    new VerificationChainOutput(multiExportResult, pcompResult, punfResult, mpsatResult,
                            verificationParameters, "Output determinacy holds."));

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
        for (Place place : stg.getPlaces()) {
            Set<String> postsetEvents = new HashSet<>();
            for (MathNode node : stg.getPostset(place)) {
                String ref = stg.getNodeReference(node);
                Pair<String, Integer> pair = LabelParser.parseInstancedTransition(ref);
                if ((pair != null) && !postsetEvents.add(pair.getFirst())) {
                    return false;
                }
            }
        }
        return true;
    }

    private VerificationParameters getVerificationParameters(Collection<String> shadowTransitionRefs) {
        String str = shadowTransitionRefs.stream().map(ref -> "\"" + ref + "\", ").collect(Collectors.joining());
        String reachConformationNway = REACH_OUTPUT_DETERMINACY.replace(REPLACEMENT_SHADOW_TRANSITIONS, str);

        return new VerificationParameters("Output determinacy",
                VerificationMode.STG_REACHABILITY_OUTPUT_DETERMINACY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reachConformationNway, true);
    }

}
