package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.interop.Format;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fst.interop.SgFormat;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.petrify.PetrifyUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class TransformationTask implements Task<TransformationOutput>, ExternalProcessListener {

    private final WorkspaceEntry we;
    private final List<String> args;
    private final Collection<Mutex> mutexes;

    public TransformationTask(WorkspaceEntry we, List<String> args, Collection<Mutex> mutexes) {
        this.we = we;
        this.args = args;
        this.mutexes = mutexes;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    @Override
    public Result<? extends TransformationOutput> run(ProgressMonitor<? super TransformationOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(PetrifySettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        command.addAll(args);

        // Extra arguments (should go before the file parameters)
        String extraArgs = PetrifySettings.getArgs();
        if (PetrifySettings.getAdvancedMode()) {
            String tmp = DialogUtils.showInput("Additional parameters for Petrify:", extraArgs);
            if (tmp == null) {
                return Result.cancel();
            }
            extraArgs = tmp;
        }
        command.addAll(TextUtils.splitWords(extraArgs));

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

            File stgFile = new File(directory, PetrifyUtils.STG_FILE_NAME);
            command.add("-o");
            command.add(stgFile.getAbsolutePath());

            Model model = we.getModelEntry().getMathModel();

            // Check for isolated marked places and temporary remove them is requested
            if (model instanceof PetriModel) {
                PetriModel petri = (PetriModel) model;
                HashSet<Place> isolatedPlaces = ConversionUtils.getIsolatedMarkedPlaces(petri);
                if (!isolatedPlaces.isEmpty()) {
                    String refStr = ReferenceHelper.getNodesAsWrapString(petri, isolatedPlaces);
                    String msg = "Petrify does not support isolated marked places.\n\n"
                            + "Problematic places are:\n" + refStr + "\n\n"
                            + "Proceed without these places?";
                    if (!DialogUtils.showConfirmWarning(msg, "Petrify transformation", true)) {
                        return Result.cancel();
                    }
                    we.captureMemento();
                    VisualModel visualModel = we.getModelEntry().getVisualModel();
                    ConversionUtils.removeIsolatedMarkedPlaces(visualModel);
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

            if (result.isSuccess()) {
                ExternalProcessOutput output = result.getPayload();
                String errorMessage = output.getStderrString();
                if ((output.getReturnCode() != 0) || errorMessage.contains(">>> ERROR: Cannot solve CSC.\n")) {
                    return Result.failure(new TransformationOutput(output, null));
                }

                Stg stg = stgFile.exists() ? StgUtils.importStg(stgFile) : null;
                return Result.success(new TransformationOutput(output, stg));
            }

            if (result.isCancel()) {
                return Result.cancel();
            }

            return Result.exception(result.getCause());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
            we.cancelMemento();
        }
    }

    private File getInputFile(Model model, File directory) {
        Format format = null;
        String extension = null;
        if (model instanceof PetriModel) {
            format = StgFormat.getInstance();
            extension = ".g";
        } else if (model instanceof Fsm) {
            format = SgFormat.getInstance();
            extension = ".sg";
        }
        if (format == null) {
            throw new RuntimeException("This tool is not applicable to " + model.getDisplayName() + " model.");
        }

        final Framework framework = Framework.getInstance();
        Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), model, format);
        if (exporter == null) {
            throw new NoExporterException(model, format);
        }
        File file = new File(directory, StgUtils.SPEC_FILE_PREFIX + extension);
        ExportTask exportTask = new ExportTask(exporter, model, file);
        Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(exportTask, "Exporting model");
        if (!exportResult.isSuccess()) {
            throw new RuntimeException("Unable to export the model.");
        }
        if ((mutexes != null) && !mutexes.isEmpty()) {
            Stg stg = StgUtils.loadStg(file);
            MutexUtils.factoroutMutexs(stg, mutexes);
            file = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + extension);
            exportTask = new ExportTask(exporter, stg, file);
            exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
            if (!exportResult.isSuccess()) {
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
