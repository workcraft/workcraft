package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petrify.PetrifyUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TransformationTask implements Task<TransformationResult>, ExternalProcessListener {
    private static final String MESSAGE_EXPORT_FAILED = "Unable to export the model.";
    private final WorkspaceEntry we;
    String[] args;

    public TransformationTask(WorkspaceEntry we, String description, String[] args) {
        this.we = we;
        this.args = args;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    @Override
    public Result<? extends TransformationResult> run(ProgressMonitor<? super TransformationResult> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ToolUtils.getAbsoluteCommandPath(PetrifyUtilitySettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = PetrifyUtilitySettings.getArgs();
        if (PetrifyUtilitySettings.getAdvancedMode()) {
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            String tmp = JOptionPane.showInputDialog(mainWindow, "Additional parameters for Petrify:", extraArgs);
            if (tmp == null) {
                return Result.cancelled();
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
            File logFile = new File(directory, "petrify.log");
            command.add("-log");
            command.add(logFile.getAbsolutePath());

            File outFile = new File(directory, "result.g");
            command.add("-o");
            command.add(outFile.getAbsolutePath());

            Model model = we.getModelEntry().getMathModel();

            // Check for isolated marked places and temporary remove them is requested
            if (model instanceof PetriNetModel) {
                PetriNetModel petri = (PetriNetModel) model;
                HashSet<Place> isolatedPlaces = PetriNetUtils.getIsolatedMarkedPlaces(petri);
                if (!isolatedPlaces.isEmpty()) {
                    String refStr = ReferenceHelper.getNodesAsString(petri, (Collection) isolatedPlaces);
                    int answer = JOptionPane.showConfirmDialog(Framework.getInstance().getMainWindow(),
                            "Petrify does not support isolated marked places.\n\n"
                                    + "Problematic places are:\n" + refStr + "\n\n"
                                    + "Proceed without these places?",
                            "Petrify transformation", JOptionPane.YES_NO_OPTION);
                    if (answer != JOptionPane.YES_OPTION) {
                        return Result.cancelled();
                    }
                    we.captureMemento();
                    VisualModel visualModel = we.getModelEntry().getVisualModel();
                    PetriNetUtils.removeIsolatedMarkedPlaces(visualModel);
                }
            }

            // Input file
            File modelFile = getInputFile(model, directory);
            command.add(modelFile.getAbsolutePath());

            boolean printStdout = PetrifyUtilitySettings.getPrintStdout();
            boolean printStderr = PetrifyUtilitySettings.getPrintStderr();
            ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessResult> res = task.run(mon);

            if (res.getOutcome() == Outcome.FINISHED) {
                StgModel outStg = null;
                if (outFile.exists()) {
                    String out = FileUtils.readAllText(outFile);
                    ByteArrayInputStream outStream = new ByteArrayInputStream(out.getBytes());
                    try {
                        outStg = new DotGImporter().importSTG(outStream);
                    } catch (DeserialisationException e) {
                        return Result.exception(e);
                    }
                }
                TransformationResult result = new TransformationResult(res, outStg);
                if (res.getReturnValue().getReturnCode() == 0) {
                    return Result.finished(result);
                } else {
                    return Result.failed(result);
                }
            }
            if (res.getOutcome() == Outcome.CANCELLED) {
                return Result.cancelled();
            }
            return Result.failed(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
            we.cancelMemento();
        }
    }

    private File getInputFile(Model model, File directory) {
        final Framework framework = Framework.getInstance();
        UUID format = null;
        String extension = null;
        if (model instanceof PetriNetModel) {
            format = Format.STG;
            extension = ".g";
        } else if (model instanceof Fsm) {
            format = Format.SG;
            extension = ".sg";
        }
        if (format == null) {
            throw new RuntimeException("This tool is not applicable to " + model.getDisplayName() + " model.");
        }

        File modelFile = new File(directory, "original" + extension);
        try {
            ExportTask exportTask = Export.createExportTask(model, modelFile, format, framework.getPluginManager());
            Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, "Exporting model");
            if (exportResult.getOutcome() != Outcome.FINISHED) {
                throw new RuntimeException(MESSAGE_EXPORT_FAILED);
            }
        } catch (SerialisationException e) {
            throw new RuntimeException(MESSAGE_EXPORT_FAILED);
        }
        return modelFile;
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
