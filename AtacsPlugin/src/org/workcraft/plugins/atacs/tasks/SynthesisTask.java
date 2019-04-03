package org.workcraft.plugins.atacs.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.atacs.AtacsSettings;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.ExportTask;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.LpnFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class SynthesisTask implements Task<SynthesisOutput>, ExternalProcessListener {
    private final WorkspaceEntry we;
    private final String[] args;
    private final Collection<Mutex> mutexes;

    public SynthesisTask(WorkspaceEntry we, String[] args, Collection<Mutex> mutexes) {
        this.we = we;
        this.args = args;
        this.mutexes = mutexes;
    }

    @Override
    public Result<? extends SynthesisOutput> run(ProgressMonitor<? super SynthesisOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(AtacsSettings.getCommand());
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
                String verilog = getFileContent(verilogFile);
                if (output.getReturnCode() != 0) {
                    return Result.failure(new SynthesisOutput(output, verilog));
                }
                return Result.success(new SynthesisOutput(output, verilog));
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
