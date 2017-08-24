package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifySynthesisTask implements Task<PetrifySynthesisResult>, ExternalProcessListener {
    private final WorkspaceEntry we;
    private final String[] args;
    private final Collection<Mutex> mutexes;

    /**
     * @param args - arguments corresponding to type of logic synthesis
     * @param inputFile - specification (STG)
     * @param equationsFile - equation Output in EQN format (not BLIF format)
     * @param libraryFile - could be null
     * @param logFile - could be null
     */
    public PetrifySynthesisTask(WorkspaceEntry we, String[] args, Collection<Mutex> mutexes) {
        this.we = we;
        this.args = args;
        this.mutexes = mutexes;
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
            String tmp = DialogUtils.showInput("Additional parameters for Petrify:", extraArgs);
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

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        // Check for isolated marked places and temporary remove them if requested
        HashSet<Place> isolatedPlaces = PetriNetUtils.getIsolatedMarkedPlaces(stg);
        if (!isolatedPlaces.isEmpty()) {
            String refStr = ReferenceHelper.getNodesAsString(stg, (Collection) isolatedPlaces, 50);
            String msg = "Petrify does not support isolated marked places.\n\n"
                    + "Problematic places are:\n" + refStr + "\n\n"
                    + "Proceed without these places?";
            if (!DialogUtils.showConfirm(msg, "Petrify synthesis")) {
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
        Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), stg, StgFormat.getInstance());
        if (stgExporter == null) {
            throw new RuntimeException("Exporter not available: model class " + stg.getClass().getName() + " to format STG.");
        }

        File stgFile = new File(directory, "spec" + StgFormat.getInstance().getExtension());
        ExportTask exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
        Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
        if (exportResult.getOutcome() != Outcome.FINISHED) {
            throw new RuntimeException("Unable to export the model.");
        }
        if (!mutexes.isEmpty()) {
            stg = StgUtils.loadStg(stgFile);
            for (Mutex mutex: mutexes) {
                LogUtils.logInfo("Factored out " + mutex);
                setMutexRequest(stg, mutex.r1);
                stg.setSignalType(mutex.g1.name, Type.INPUT);
                setMutexRequest(stg, mutex.r2);
                stg.setSignalType(mutex.g2.name, Type.INPUT);
            }
            stgFile = new File(directory, "spec-mutex" + StgFormat.getInstance().getExtension());
            exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
            exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
            if (exportResult.getOutcome() != Outcome.FINISHED) {
                throw new RuntimeException("Unable to export the model after factoring out the mutexes.");
            }
        }
        return stgFile;
    }

    private void setMutexRequest(Stg stg, Signal signal) {
        if (signal.type == Type.INTERNAL) {
            LogUtils.logInfo("Internal signal " + signal.name + " is temporary changed to output, so it is not optimised away.");
            stg.setSignalType(signal.name, Type.OUTPUT);
        }
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
