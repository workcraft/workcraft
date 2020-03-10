package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.circuit.CircuitSettings;
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
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class SynthesisTask implements Task<SynthesisOutput>, ExternalProcessListener {
    private final WorkspaceEntry we;
    private final List<String> args;
    private final Collection<Mutex> mutexes;
    private final boolean needsGateLibrary;

    public SynthesisTask(WorkspaceEntry we, List<String> args, Collection<Mutex> mutexes, boolean needsGateLibrary) {
        this.we = we;
        this.args = args;
        this.mutexes = mutexes;
        this.needsGateLibrary = needsGateLibrary;
    }

    @Override
    public Result<? extends SynthesisOutput> run(ProgressMonitor<? super SynthesisOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(PetrifySettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        command.addAll(args);

        // Technology mapping library (if needed and accepted)
        String gateLibrary = ExecutableUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
        if (needsGateLibrary) {
            if ((gateLibrary == null) || gateLibrary.isEmpty()) {
                return Result.exception(new IOException("Gate library is not specified.\n" +
                        "Check '" + CircuitSettings.GATE_LIBRARY_TITLE + "' item in Digital Circuit preferences."));
            }
            File gateLibraryFile = new File(gateLibrary);
            if (!FileUtils.checkAvailability(gateLibraryFile, "Gate library access error", false)) {
                return Result.exception(new IOException("Cannot find gate library file '" + gateLibrary + "'.\n" +
                        "Check '" + CircuitSettings.GATE_LIBRARY_TITLE + "' item in Digital Circuit preferences."));
            }
            command.add("-lib");
            command.add(gateLibraryFile.getAbsolutePath());
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
        HashSet<Place> isolatedPlaces = ConversionUtils.getIsolatedMarkedPlaces(stg);
        if (!isolatedPlaces.isEmpty()) {
            String refStr = ReferenceHelper.getNodesAsString(stg, isolatedPlaces, SizeHelper.getWrapLength());
            String msg = "Petrify does not support isolated marked places.\n\n"
                    + "Problematic places are:\n" + refStr + "\n\n"
                    + "Proceed without these places?";
            if (!DialogUtils.showConfirmWarning(msg, "Petrify synthesis", true)) {
                return Result.cancelation();
            }
            we.captureMemento();
            VisualModel visualModel = we.getModelEntry().getVisualModel();
            ConversionUtils.removeIsolatedMarkedPlaces(visualModel);
        }

        // Input file
        File stgFile = getInputFile(stg, directory);
        command.add(stgFile.getAbsolutePath());

        boolean printStdout = PetrifySettings.getPrintStdout();
        boolean printStderr = PetrifySettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        try {
            if (result.getOutcome() == Outcome.SUCCESS) {
                ExternalProcessOutput output = result.getPayload();
                if (output != null) {
                    String log = getFileContent(logFile);
                    String equations = getFileContent(eqnFile);
                    String verilog = getFileContent(verilogFile);
                    String stgOutput = getFileContent(outFile);
                    SynthesisOutput synthesisOutput = new SynthesisOutput(output, log, equations, verilog, stgOutput);
                    if (output.getReturnCode() == 0) {
                        return Result.success(synthesisOutput);
                    }
                    return Result.failure(synthesisOutput);
                }
            }

            if (result.getOutcome() == Outcome.CANCEL) {
                return Result.cancelation();
            }

            return Result.exception(result.getCause());
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
            MutexUtils.factoroutMutexs(stg, mutexes);
            stgFile = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + gExtension);
            exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
            exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
            if (exportResult.getOutcome() != Outcome.SUCCESS) {
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
