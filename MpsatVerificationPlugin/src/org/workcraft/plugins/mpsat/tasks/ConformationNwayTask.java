package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.mpsat.utils.TransformUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConformationNwayTask implements Task<VerificationChainOutput> {

    private static final String REPLACEMENT_SHADOW_TRANSITIONS =
            "/* insert set of names of shadow transitions here */"; // For example: "x+/1", "x-", "y+", "y-/1"

    private static final String REACH_CONFORMATION_NWAY =
            "// Check whether several STGs conform to each other.\n" +
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
            "    // Check if some output signal is enabled due to phantom transitions only;\n" +
            "    // this would mean that some component STG does not conform to the rest of the composition.\n" +
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

    private final List<WorkspaceEntry> wes;

    public ConformationNwayTask(List<WorkspaceEntry> wes) {
        this.wes = wes;
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();

        String prefix = FileUtils.getTempPrefix("-nway_conformation");
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        VerificationParameters preparationSettings = ReachUtils.getToolchainPreparationSettings();
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
                            new VerificationChainOutput(exportResult, null, null, null, preparationSettings));
                }
            }
            Result<MultiSubExportOutput> multiExportResult = new Result<>(new MultiSubExportOutput(stgFiles, substitutes));
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
                        new VerificationChainOutput(multiExportResult, pcompResult, null, null, preparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Insert shadow transitions into the composed STG
            CompositionData compositionData = new CompositionData(detailFile);
            Stg modSysStg = StgUtils.importStg(sysStgFile);
            Set<String> shadowTransitions = TransformUtils.generateShadows(modSysStg, compositionData);
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
                        new VerificationChainOutput(modSysExportResult, pcompResult, punfResult, null, preparationSettings));
            }
            monitor.progressUpdate(0.60);

            // Check for conformation
            VerificationParameters mpsatSettings = getSettings(shadowTransitions);
            VerificationTask verificationTask = new VerificationTask(mpsatSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, sysStgFile);
            Result<? extends VerificationOutput>  mpsatResult = taskManager.execute(
                    verificationTask, "Running conformation check [MPSat]", new SubtaskMonitor<>(monitor));

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new VerificationChainOutput(modSysExportResult, pcompResult, punfResult, mpsatResult, mpsatSettings));
            }
            monitor.progressUpdate(0.80);

            VerificationOutputParser mpsatParser = new VerificationOutputParser(mpsatResult.getPayload());
            if (!mpsatParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new VerificationChainOutput(multiExportResult, pcompResult, punfResult, mpsatResult, mpsatSettings,
                                "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            String message = "N-way conformation holds.";
            return new Result<>(Outcome.SUCCESS,
                    new VerificationChainOutput(multiExportResult, pcompResult, punfResult, mpsatResult, mpsatSettings, message));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private VerificationParameters getSettings(Set<String> shadowTransitionNames) {
        String str = shadowTransitionNames.stream().map(ref -> "\"" + ref + "\", ").collect(Collectors.joining());
        String reachConformationNway = REACH_CONFORMATION_NWAY.replace(REPLACEMENT_SHADOW_TRANSITIONS, str);

        return new VerificationParameters("N-way conformation",
                VerificationMode.STG_REACHABILITY_CONFORMATION_NWAY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reachConformationNway, true);
    }

}
