package org.workcraft.plugins.atacs.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.atacs.AtacsSettings;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.LpnFormat;
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

public class AtacsSynthesisTask implements Task<AtacsSynthesisOutput>, ExternalProcessListener {
    private final WorkspaceEntry we;
    private final String[] args;
    private final Collection<Mutex> mutexes;

    public AtacsSynthesisTask(WorkspaceEntry we, String[] args, Collection<Mutex> mutexes) {
        this.we = we;
        this.args = args;
        this.mutexes = mutexes;
    }

    @Override
    public Result<? extends AtacsSynthesisOutput> run(ProgressMonitor<? super AtacsSynthesisOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ToolUtils.getAbsoluteCommandPath(AtacsSettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = AtacsSettings.getArgs();
        if (AtacsSettings.getAdvancedMode()) {
            String tmp = DialogUtils.showInput("Additional parameters for ATACS:", extraArgs);
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

        command.add("-ys"); // Do circuit synthesis
        command.add("-sV"); // Output synthesis result in Verilog format

        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        // Input file
        File stgFile = getInputFile(stg, directory);
        command.add(stgFile.getAbsolutePath());
        File verilogFile = new File(directory, stgFile.getName().replace(".lpn", ".v"));

        boolean printStdout = AtacsSettings.getPrintStdout();
        boolean printStderr = AtacsSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        try {
            if (result.getOutcome() == Outcome.SUCCESS) {
                ExternalProcessOutput output = result.getPayload();
                String verilog = getFileContent(verilogFile);
                if (output.getReturnCode() != 0) {
                    return Result.failure(new AtacsSynthesisOutput(output, verilog));
                }
                return Result.success(new AtacsSynthesisOutput(output, verilog));
            }
            if (result.getOutcome() == Outcome.CANCEL) {
                return Result.cancelation();
            }
            return Result.failure();
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
        LpnFormat format = LpnFormat.getInstance();
        Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), stg, format);
        if (exporter == null) {
            throw new NoExporterException(stg, format);
        }

        String extension = format.getExtension();
        File file = new File(directory, StgUtils.SPEC_FILE_PREFIX + extension);
        ExportTask exportTask = new ExportTask(exporter, stg, file.getAbsolutePath());
        Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(exportTask, "Exporting .lpn");
        if (exportResult.getOutcome() != Outcome.SUCCESS) {
            throw new RuntimeException("Unable to export the model.");
        }
        if (!mutexes.isEmpty()) {
            stg = StgUtils.loadStg(file);
            for (Mutex mutex: mutexes) {
                LogUtils.logInfo("Factored out " + mutex);
                setMutexRequest(stg, mutex.r1);
                stg.setSignalType(mutex.g1.name, Signal.Type.INPUT);
                setMutexRequest(stg, mutex.r2);
                stg.setSignalType(mutex.g2.name, Signal.Type.INPUT);
            }
            file = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + extension);
            exportTask = new ExportTask(exporter, stg, file.getAbsolutePath());
            exportResult = framework.getTaskManager().execute(exportTask, "Exporting .lpn");
            if (exportResult.getOutcome() != Outcome.SUCCESS) {
                throw new RuntimeException("Unable to export the model after factoring out the mutexes.");
            }
        }
        return file;
    }

    private void setMutexRequest(Stg stg, Signal signal) {
        if (signal.type == Signal.Type.INTERNAL) {
            LogUtils.logInfo("Internal signal " + signal.name + " is temporary changed to output, so it is not optimised away.");
            stg.setSignalType(signal.name, Signal.Type.OUTPUT);
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
