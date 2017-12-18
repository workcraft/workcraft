package org.workcraft.plugins.mpsat.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatNwayConformationTask implements Task<MpsatChainResult> {

    private final MpsatParameters toolchainPreparationSettings = new MpsatParameters("Toolchain preparation of data",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final MpsatParameters toolchainCompletionSettings = new MpsatParameters("Toolchain completion",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final ArrayList<WorkspaceEntry> wes;

    public MpsatNwayConformationTask(ArrayList<WorkspaceEntry> wes) {
        this.wes = wes;
    }

    @Override
    public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
        Framework framework = Framework.getInstance();

        String prefix = FileUtils.getTempPrefix("-pcomp");
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        try {
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends Object> exportResult = null;
            Collection<Stg> stgs = new ArrayList<>();
            Collection<File> stgFiles = new ArrayList<>();
            ArrayList<Set<String>> allOutputSets = new ArrayList<>();
            for (WorkspaceEntry we: wes) {
                Stg stg = WorkspaceUtils.getAs(we, Stg.class);
                stgs.add(stg);
                Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), stg, StgFormat.getInstance());
                if (stgExporter == null) {
                    throw new RuntimeException("Exporter not available: model class " + stg.getClass().getName() + " to format STG.");
                }
                Set<String> outputSignalNames = stg.getSignalNames(Type.OUTPUT, null);
                allOutputSets.add(outputSignalNames);

                // Generating .g for the model
                File stgFile = new File(directory, we.getTitle() + stgFileExtension);
                stgFiles.add(stgFile);
                ExportTask exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
                exportResult = framework.getTaskManager().execute(
                        exportTask, "Exporting circuit .g", subtaskMonitor);

                if (exportResult.getOutcome() != Outcome.SUCCESS) {
                    if (exportResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<MpsatChainResult>(Outcome.CANCEL);
                    }
                    return new Result<MpsatChainResult>(Outcome.FAILURE,
                            new MpsatChainResult(exportResult, null, null, null, toolchainPreparationSettings));
                }
            }
            monitor.progressUpdate(0.30);

            // Generating .g for the whole system (model and environment)
            File placesFile = new File(directory, StgUtils.PLACES_FILE_NAME);
            File stgFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + stgFileExtension);
            stgFile.deleteOnExit();
            PcompTask pcompTask = new PcompTask(stgFiles.toArray(new File[0]), stgFile, placesFile,
                    ConversionMode.OUTPUT, false, false, directory);

            Result<? extends ExternalProcessResult> pcompResult = framework.getTaskManager().execute(
                    pcompTask, "Running parallel composition [PComp]", subtaskMonitor);

            if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                if (pcompResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, pcompResult, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Generate unfolding
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + PunfSettings.getUnfoldingExtension(true));
            PunfTask punfTask = new PunfTask(stgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(
                    punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.60);

            // Check for interface conformation
            byte[] palcesList = FileUtils.readAllBytes(placesFile);
            ArrayList<Set<String>> allPlaceSets = parsePlaceNames(palcesList);

            MpsatParameters conformationSettings = MpsatParameters.getNwayConformationSettings(allPlaceSets, allOutputSets);
            MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, true, stgFile, placesFile);
            Result<? extends ExternalProcessResult>  mpsatConformationResult = framework.getTaskManager().execute(
                    mpsatConformationTask, "Running conformation check [MPSat]", subtaskMonitor);

            if (mpsatConformationResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatConformationResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings));
            }
            monitor.progressUpdate(0.80);

            MpsatResultParser mpsatConformationParser = new MpsatResultParser(mpsatConformationResult.getReturnValue());
            if (!mpsatConformationParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainResult>(Outcome.SUCCESS,
                        new MpsatChainResult(exportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings,
                                "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            unfoldingFile.delete();
            String message = "All models conform to each other.";
            return new Result<MpsatChainResult>(Outcome.SUCCESS,
                    new MpsatChainResult(exportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private ArrayList<Set<String>> parsePlaceNames(byte[] bufferedInput) {
        ArrayList<Set<String>> result = new ArrayList<>();
        InputStream is = new ByteArrayInputStream(bufferedInput);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                Set<String> names = new HashSet<>();
                for (String name: line.trim().split("\\s")) {
                    names.add(name);
                }
                result.add(names);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
