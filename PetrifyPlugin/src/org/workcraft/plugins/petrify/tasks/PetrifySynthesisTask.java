package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.petrify.PetrifyUtils;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
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
import org.workcraft.util.ExportUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifySynthesisTask implements Task<PetrifySynthesisResult>, ExternalProcessListener {
    private final WorkspaceEntry we;
    private final String[] args;
    private final Collection<Mutex> mutexes;

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
                return Result.cancelation();
            }
            extraArgs = tmp;
        }
        for (String arg : extraArgs.split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        // Petrify uses the full name of the file as the name of the Verilog module (with _net suffix).
        // As there may be non-alpha-numerical symbols in the model title, it needs to be preprocessed
        // before using it in the directory name.
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);

        File outFile = null;
        if (!PetrifySettings.getWriteStg() && !PetrifySettings.getOpenSynthesisStg()) {
            command.add("-no");
        } else {
            outFile = new File(directory, PetrifyUtils.STG_FILE_NAME);
            command.add("-o");
            command.add(outFile.getAbsolutePath());
        }

        File logFile = null;
        if (!PetrifySettings.getWriteLog()) {
            command.add("-nolog");
        } else {
            logFile = new File(directory, PetrifyUtils.LOG_FILE_NAME);
            command.add("-log");
            command.add(logFile.getAbsolutePath());
        }

        File eqnFile = null;
        if (PetrifySettings.getWriteEqn()) {
            eqnFile = new File(directory, PetrifyUtils.EQN_FILE_NAME);
            command.add("-eqn");
            command.add(eqnFile.getAbsolutePath());
        }

        File verilogFile = new File(directory, PetrifyUtils.VERILOG_FILE_NAME);
        command.add("-vl");
        command.add(verilogFile.getAbsolutePath());

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        // Check for isolated marked places and temporary remove them if requested
        HashSet<Place> isolatedPlaces = PetriNetUtils.getIsolatedMarkedPlaces(stg);
        if (!isolatedPlaces.isEmpty()) {
            String refStr = ReferenceHelper.getNodesAsString(stg, isolatedPlaces, 50);
            String msg = "Petrify does not support isolated marked places.\n\n"
                    + "Problematic places are:\n" + refStr + "\n\n"
                    + "Proceed without these places?";
            if (!DialogUtils.showConfirmWarning(msg, "Petrify synthesis", true)) {
                return Result.cancelation();
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
        Result<? extends ExternalProcessOutput> res = task.run(mon);
        try {
            if (res.getOutcome() == Outcome.SUCCESS) {
                String log = getFileContent(logFile);
                String equations = getFileContent(eqnFile);
                String verilog = getFileContent(verilogFile);
                String out = getFileContent(outFile);
                String stdout = new String(res.getPayload().getStdout());
                String stderr = new String(res.getPayload().getStderr());
                PetrifySynthesisResult result = new PetrifySynthesisResult(log, equations, verilog, out, stdout, stderr);
                if (res.getPayload().getReturnCode() == 0) {
                    return Result.success(result);
                } else {
                    return Result.failure(result);
                }
            }
            if (res.getOutcome() == Outcome.CANCEL) {
                return Result.cancelation();
            }
            return Result.failure(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
            we.cancelMemento();
        }
    }

    private String getFileContent(File file) throws IOException {
        return (file != null) && file.exists() ? FileUtils.readAllText(file) : "";
    }

    private File getInputFile(Stg stg, File directory) {
        final Framework framework = Framework.getInstance();
        StgFormat format = StgFormat.getInstance();
        Exporter stgExporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), stg, format);
        if (stgExporter == null) {
            throw new NoExporterException(stg, format);
        }

        String gExtension = format.getExtension();
        File stgFile = new File(directory, StgUtils.SPEC_FILE_PREFIX + gExtension);
        ExportTask exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
        Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
        if (exportResult.getOutcome() != Outcome.SUCCESS) {
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
            stgFile = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + gExtension);
            exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
            exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
            if (exportResult.getOutcome() != Outcome.SUCCESS) {
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
