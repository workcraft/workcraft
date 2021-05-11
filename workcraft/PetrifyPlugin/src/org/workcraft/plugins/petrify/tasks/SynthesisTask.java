package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.utils.VerilogUtils;
import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class SynthesisTask implements Task<SynthesisOutput>, ExternalProcessListener {

    private static final String STG_FILE_NAME = "petrify.g";
    private static final String LOG_FILE_NAME = "petrify.log";
    private static final String EQN_FILE_NAME = "petrify.eqn";
    private static final String VERILOG_FILE_NAME = "petrify.v";

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
                return Result.cancel();
            }
            extraArgs = tmp;
        }
        command.addAll(TextUtils.splitWords(extraArgs));

        // Petrify uses the full name of the file as the name of the Verilog module (with _net suffix).
        // As there may be non-alpha-numerical symbols in the model title, it needs to be preprocessed
        // before using it in the directory name.
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);

        File outFile = null;
        if (PetrifySettings.getOpenSynthesisStg()) {
            outFile = new File(directory, STG_FILE_NAME);
            command.add("-o");
            command.add(outFile.getAbsolutePath());
        } else {
            command.add("-no");
        }

        File logFile = null;
        if (PetrifySettings.getWriteLog()) {
            logFile = new File(directory, LOG_FILE_NAME);
            command.add("-log");
            command.add(logFile.getAbsolutePath());
        } else {
            command.add("-nolog");
        }

        File eqnFile = null;
        if (PetrifySettings.getWriteEqn()) {
            eqnFile = new File(directory, EQN_FILE_NAME);
            command.add("-eqn");
            command.add(eqnFile.getAbsolutePath());
        }

        File verilogFile = new File(directory, VERILOG_FILE_NAME);
        command.add("-vl");
        command.add(verilogFile.getAbsolutePath());

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        // Check for isolated marked places and temporary remove them if requested
        HashSet<Place> isolatedPlaces = PetriUtils.getIsolatedMarkedPlaces(stg);
        if (!isolatedPlaces.isEmpty()) {
            String refStr = ReferenceHelper.getNodesAsWrapString(stg, isolatedPlaces);
            String msg = "Petrify does not support isolated marked places.\n\n"
                    + "Problematic places are:\n" + refStr + "\n\n"
                    + "Proceed without these places?";
            if (!DialogUtils.showConfirmWarning(msg, "Petrify synthesis", true)) {
                return Result.cancel();
            }
            we.captureMemento();
            VisualModel visualModel = we.getModelEntry().getVisualModel();
            PetriUtils.removeIsolatedMarkedVisualPlaces(visualModel);
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
            ExternalProcessOutput output = result.getPayload();
            if (result.isSuccess() && (output != null)) {
                if (output.getReturnCode() != 0) {
                    return Result.failure(new SynthesisOutput(output, null, null));
                }

                VerilogModule verilogModule = VerilogUtils.importTopVerilogModule(verilogFile);
                Stg outStg = outFile != null && outFile.exists() ? StgUtils.importStg(outFile) : null;
                SynthesisOutput synthesisOutput = new SynthesisOutput(output, verilogModule, outStg);

                if ((logFile != null) && logFile.exists()) {
                    synthesisOutput.setLog(FileUtils.readAllText(logFile));
                }
                if ((eqnFile != null) && eqnFile.exists()) {
                    synthesisOutput.setEquations(FileUtils.readAllText(eqnFile));
                }
                return Result.success(synthesisOutput);
            }

            if (result.isCancel()) {
                return Result.cancel();
            }

            return Result.exception(result.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
            we.cancelMemento();
        }
    }

    private File getInputFile(Stg stg, File directory) {
        StgFormat format = StgFormat.getInstance();
        Exporter stgExporter = ExportUtils.chooseBestExporter(stg, format);
        if (stgExporter == null) {
            throw new NoExporterException(stg, format);
        }

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        String gExtension = format.getExtension();
        File stgFile = new File(directory, StgUtils.SPEC_FILE_PREFIX + gExtension);
        ExportTask exportTask = new ExportTask(stgExporter, stg, stgFile);
        Result<? extends ExportOutput> exportResult = taskManager.execute(exportTask, "Exporting .g");
        if (!exportResult.isSuccess()) {
            throw new RuntimeException("Unable to export the model.");
        }
        if (!mutexes.isEmpty()) {
            stg = StgUtils.loadStg(stgFile);
            MutexUtils.factoroutMutexes(stg, mutexes);
            stgFile = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + gExtension);
            exportTask = new ExportTask(stgExporter, stg, stgFile);
            exportResult = taskManager.execute(exportTask, "Exporting .g");
            if (!exportResult.isSuccess()) {
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
