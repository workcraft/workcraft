package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.MutexData;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
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
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifySynthesisTask implements Task<PetrifySynthesisResult>, ExternalProcessListener {
    private final WorkspaceEntry we;
    private final String[] args;
    private final Collection<MutexData> mutexData;

    /**
     * @param args - arguments corresponding to type of logic synthesis
     * @param inputFile - specification (STG)
     * @param equationsFile - equation Output in EQN format (not BLIF format)
     * @param libraryFile - could be null
     * @param logFile - could be null
     */
    public PetrifySynthesisTask(WorkspaceEntry we, String[] args, Collection<MutexData> mutexData) {
        this.we = we;
        this.args = args;
        this.mutexData = mutexData;
    }

    @Override
    public Result<? extends PetrifySynthesisResult> run(ProgressMonitor<? super PetrifySynthesisResult> monitor) {
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

        // Petrify uses the full name of the file as the name of the Verilog module (with _net suffix).
        // As there may be non-alpha-numerical symbols in the model title, it is better not to include it to the directory name.
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);

        File outFile = new File(directory, "result.g");
        command.add("-o");
        command.add(outFile.getAbsolutePath());

        File equationsFile = new File(directory, "petrify.eqn");
        command.add("-eqn");
        command.add(equationsFile.getAbsolutePath());

        File verilogFile = new File(directory, "petrify.v");
        command.add("-vl");
        command.add(verilogFile.getAbsolutePath());

        File blifFile = new File(directory, "petrify.blif");
        command.add("-blif");
        command.add(blifFile.getAbsolutePath());

        File logFile = new File(directory, "petrify.log");
        command.add("-log");
        command.add(logFile.getAbsolutePath());

        // Preserve internal signals (hidden option)
        //command.add("-keepinternal");

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        // Check for isolated marked places and temporary remove them is requested
        HashSet<Place> isolatedPlaces = PetriNetUtils.getIsolatedMarkedPlaces(stg);
        if (!isolatedPlaces.isEmpty()) {
            String refStr = ReferenceHelper.getNodesAsString(stg, (Collection) isolatedPlaces, 50);
            int answer = JOptionPane.showConfirmDialog(Framework.getInstance().getMainWindow(),
                    "Petrify does not support isolated marked places.\n\n"
                            + "Problematic places are:\n" + refStr + "\n\n"
                            + "Proceed without these places?",
                    "Petrify synthesis", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return Result.cancelled();
            }
            we.captureMemento();
            VisualModel visualModel = we.getModelEntry().getVisualModel();
            PetriNetUtils.removeIsolatedMarkedPlaces(visualModel);
        }

        // Input file
        File stgFile = getInputFile(stg, directory);
        command.add(stgFile.getAbsolutePath());

        boolean printStdout = PetrifySettings.getPrintStdout();
        boolean printStderr = PetrifySettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, null, printStdout, printStderr);
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessResult> res = task.run(mon);
        try {
            if (res.getOutcome() == Outcome.FINISHED) {
                String equations = equationsFile.exists() ? FileUtils.readAllText(equationsFile) : "";
                String verilog = verilogFile.exists() ? FileUtils.readAllText(verilogFile) : "";
                String log = logFile.exists() ? FileUtils.readAllText(logFile) : "";
                String stdout = new String(res.getReturnValue().getOutput());
                String stderr = new String(res.getReturnValue().getErrors());
                PetrifySynthesisResult result = new PetrifySynthesisResult(equations, verilog, log, stdout, stderr);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
            we.cancelMemento();
        }
    }

    private File getInputFile(Stg stg, File directory) {
        final Framework framework = Framework.getInstance();
        Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), stg, Format.STG);
        if (stgExporter == null) {
            throw new RuntimeException("Exporter not available: model class " + stg.getClass().getName() + " to format STG.");
        }

        File stgFile = new File(directory, "spec" + stgExporter.getExtenstion());
        ExportTask exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
        Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
        if (exportResult.getOutcome() != Outcome.FINISHED) {
            throw new RuntimeException("Unable to export the model.");
        }
        if (!mutexData.isEmpty()) {
            stg = StgUtils.loadStg(stgFile);
            for (MutexData m: mutexData) {
                stg.setSignalType(m.g1, Type.INPUT);
                stg.setSignalType(m.g2, Type.INPUT);
            }
            stgFile = new File(directory, "spec-mutex" + stgExporter.getExtenstion());
            exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
            exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
            if (exportResult.getOutcome() != Outcome.FINISHED) {
                throw new RuntimeException("Unable to export the model after factoring out the mutexes.");
            }
        }
        return stgFile;
    }

    @Override
    public void processFinished(int returnCode) {
    }

    @Override
    public void errorData(byte[] data) {
    }

    @Override
    public void outputData(byte[] data) {
    }

}
