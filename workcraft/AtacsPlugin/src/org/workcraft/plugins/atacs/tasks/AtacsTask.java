package org.workcraft.plugins.atacs.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.atacs.AtacsSettings;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.LpnFormat;
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
import java.util.List;

public class AtacsTask implements Task<AtacsOutput>, ExternalProcessListener {
    private final WorkspaceEntry we;
    private final List<String> args;
    private final Collection<Mutex> mutexes;

    public AtacsTask(WorkspaceEntry we, List<String> args, Collection<Mutex> mutexes) {
        this.we = we;
        this.args = args;
        this.mutexes = mutexes;
    }

    @Override
    public Result<? extends AtacsOutput> run(ProgressMonitor<? super AtacsOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(AtacsSettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        command.addAll(args);

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

        command.add("-oq"); // Quiet mode
        command.add("-ll"); // Load LPN as Petri net (instead of Timed Event/Rule Structure, which is default semantics)
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
                if (output != null) {
                    String verilog = getFileContent(verilogFile);
                    AtacsOutput atacsOutput = new AtacsOutput(output, verilog);
                    if (output.getReturnCode() == 0) {
                        return Result.success(atacsOutput);
                    }
                    return Result.failure(atacsOutput);
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
            MutexUtils.factoroutMutexs(stg, mutexes);
            file = new File(directory, StgUtils.SPEC_FILE_PREFIX + StgUtils.MUTEX_FILE_SUFFIX + extension);
            exportTask = new ExportTask(exporter, stg, file.getAbsolutePath());
            exportResult = framework.getTaskManager().execute(exportTask, "Exporting .lpn");
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
    }

    @Override
    public void outputData(byte[] data) {
    }

}
