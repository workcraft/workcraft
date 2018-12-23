package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.interop.Format;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fst.interop.SgFormat;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.petrify.PetrifyUtils;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class PetrifyTransformationTask implements Task<PetrifyTransformationOutput>, ExternalProcessListener {

    private final WorkspaceEntry we;
    String[] args;
    private final Collection<Mutex> mutexes;

    public PetrifyTransformationTask(WorkspaceEntry we, String description, String[] args, Collection<Mutex> mutexes) {
        this.we = we;
        this.args = args;
        this.mutexes = mutexes;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    @Override
    public Result<? extends PetrifyTransformationOutput> run(ProgressMonitor<? super PetrifyTransformationOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ToolUtils.getAbsoluteCommandPath(PetrifySettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = PetrifySettings.getArgs();
        if (PetrifySettings.getAdvancedMode()) {
            String tmp = DialogUtils.showInput("Additional parameters for Petrify:", extraArgs);
            if (tmp == null) {
                return Result.cancelation();
            }
            extraArgs = tmp;
        }
        for (String arg : extraArgs.split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            File logFile = null;
            if (!PetrifySettings.getWriteLog()) {
                command.add("-nolog");
            } else {
                logFile = new File(directory, PetrifyUtils.LOG_FILE_NAME);
                command.add("-log");
                command.add(logFile.getAbsolutePath());
            }

            File outFile = new File(directory, PetrifyUtils.STG_FILE_NAME);
            command.add("-o");
            command.add(outFile.getAbsolutePath());

            Model model = we.getModelEntry().getMathModel();

            // Check for isolated marked places and temporary remove them is requested
            if (model instanceof PetriNetModel) {
                PetriNetModel petri = (PetriNetModel) model;
                HashSet<Place> isolatedPlaces = PetriNetUtils.getIsolatedMarkedPlaces(petri);
                if (!isolatedPlaces.isEmpty()) {
                    String refStr = ReferenceHelper.getNodesAsString(petri, isolatedPlaces, SizeHelper.getWrapLength());
                    String msg = "Petrify does not support isolated marked places.\n\n"
                            + "Problematic places are:\n" + refStr + "\n\n"
                            + "Proceed without these places?";
                    if (!DialogUtils.showConfirmWarning(msg, "Petrify transformation", true)) {
                        return Result.cancelation();
                    }
                    we.captureMemento();
                    VisualModel visualModel = we.getModelEntry().getVisualModel();
                    PetriNetUtils.removeIsolatedMarkedPlaces(visualModel);
                }
            }

            // Input file
            File modelFile = getInputFile(model, directory);
            command.add(modelFile.getAbsolutePath());

            boolean printStdout = PetrifySettings.getPrintStdout();
            boolean printStderr = PetrifySettings.getPrintStderr();
            ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
            SubtaskMonitor<ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

            if (result.getOutcome() == Outcome.SUCCESS) {
                StgModel outStg = null;
                if (outFile.exists()) {
                    String out = FileUtils.readAllText(outFile);
                    ByteArrayInputStream outStream = new ByteArrayInputStream(out.getBytes());
                    try {
                        outStg = new StgImporter().importStg(outStream);
                    } catch (DeserialisationException e) {
                        return Result.exception(e);
                    }
                }
                ExternalProcessOutput output = result.getPayload();
                int returnCode = output.getReturnCode();
                String errorMessage = output.getStderrString();
                if ((returnCode != 0) || (errorMessage.contains(">>> ERROR: Cannot solve CSC.\n"))) {
                    return Result.failure(new PetrifyTransformationOutput(output, outStg));
                }
                return Result.success(new PetrifyTransformationOutput(output, outStg));
            }
            if (result.getOutcome() == Outcome.CANCEL) {
                return Result.cancelation();
            }
            return Result.failure();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
            we.cancelMemento();
        }
    }

    private File getInputFile(Model model, File directory) {
        final Framework framework = Framework.getInstance();
        Format format = null;
        String extension = null;
        if (model instanceof PetriNetModel) {
            format = StgFormat.getInstance();
            extension = ".g";
        } else if (model instanceof Fsm) {
            format = SgFormat.getInstance();
            extension = ".sg";
        }
        if (format == null) {
            throw new RuntimeException("This tool is not applicable to " + model.getDisplayName() + " model.");
        }

        File file = new File(directory, StgUtils.SPEC_FILE_PREFIX + extension);
        Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), model, format);
        if (exporter == null) {
            throw new NoExporterException(model, format);
        }
        ExportTask exportTask = new ExportTask(exporter, model, file);
        Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(exportTask, "Exporting model");
        if (exportResult.getOutcome() != Outcome.SUCCESS) {
            throw new RuntimeException("Unable to export the model.");
        }
        if ((mutexes != null) && !mutexes.isEmpty()) {
            Stg stg = StgUtils.loadStg(file);
            MutexUtils.factoroutMutexs(stg, mutexes);
            file = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + extension);
            exportTask = new ExportTask(exporter, stg, file.getAbsolutePath());
            exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
            if (exportResult.getOutcome() != Outcome.SUCCESS) {
                throw new RuntimeException("Unable to export the model after factoring out the mutexes.");
            }
        }
        return file;
    }

    @Override
    public void processFinished(int returnCode) {
    }

    @Override
    public void errorData(byte[] data) {
        System.out.print(data);
    }

    @Override
    public void outputData(byte[] data) {
        System.out.print(data);
    }

}
