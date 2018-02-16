package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.interop.Format;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fst.interop.SgFormat;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.petrify.PetrifyUtils;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.ExportUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyTransformationTask implements Task<PetrifyTransformationResult>, ExternalProcessListener {

    private final WorkspaceEntry we;
    String[] args;

    public PetrifyTransformationTask(WorkspaceEntry we, String description, String[] args) {
        this.we = we;
        this.args = args;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    @Override
    public Result<? extends PetrifyTransformationResult> run(ProgressMonitor<? super PetrifyTransformationResult> monitor) {
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
                    String refStr = ReferenceHelper.getNodesAsString(petri, isolatedPlaces, 50);
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
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessOutput> res = task.run(mon);

            if (res.getOutcome() == Outcome.SUCCESS) {
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
                PetrifyTransformationResult result = new PetrifyTransformationResult(res, outStg);
                int returnCode = res.getPayload().getReturnCode();
                String errorMessage = new String(res.getPayload().getStderr());
                if ((returnCode != 0) || (errorMessage.contains(">>> ERROR: Cannot solve CSC.\n"))) {
                    return Result.failure(result);
                }
                return Result.success(result);
            }
            if (res.getOutcome() == Outcome.CANCEL) {
                return Result.cancelation();
            }
            return Result.failure(null);
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

        File file = new File(directory, "original" + extension);
        Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), model, format);
        if (exporter == null) {
            throw new NoExporterException(model, format);
        }
        ExportTask exportTask = new ExportTask(exporter, model, file);
        Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(exportTask, "Exporting model");
        if (exportResult.getOutcome() != Outcome.SUCCESS) {
            throw new RuntimeException("Unable to export the model.");
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
