package org.workcraft.plugins.msfsm.tasks;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.fst.interop.SgFormat;
import org.workcraft.plugins.msfsm.MsfsmSettings;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ConversionTask implements Task<ConversionOutput>, ExternalProcessListener {
    private final WorkspaceEntry we;
    private final String fileName;
    private final String[] script;

    public ConversionTask(WorkspaceEntry we, String fileName, String[] script) {
        this.we = we;
        this.fileName = fileName;
        this.script = script;
    }

    @Override
    public Result<? extends ConversionOutput> run(ProgressMonitor<? super ConversionOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(MsfsmSettings.getCommand());
        command.add(toolName);

        // Extra arguments (should go before the file parameters)
        String extraArgs = MsfsmSettings.getArgs();
        if (MsfsmSettings.getAdvancedMode()) {
            String tmp = DialogUtils.showInput("Additional parameters for MSFSM:", extraArgs);
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
        Petri petri = WorkspaceUtils.getAs(we, Petri.class);

        // Script file
        if (script != null) {
            File scriptFile = getScriptFile(script, directory);
            command.add("-f");
            command.add(scriptFile.getAbsolutePath());
        }

        // Input file
        getInputFile(petri, directory);

        boolean printStdout = MsfsmSettings.getPrintStdout();
        boolean printStderr = MsfsmSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        try {
            if (result.getOutcome() == Outcome.SUCCESS) {
                ExternalProcessOutput output = result.getPayload();
                if (output.getReturnCode() != 0) {
                    return Result.failure(new ConversionOutput(output, null));
                }
                String ext = SgFormat.getInstance().getExtension();
                File[] files = directory.listFiles(file -> file.getName().endsWith(ext));
                return Result.success(new ConversionOutput(output, files));
            }
            if (result.getOutcome() == Outcome.CANCEL) {
                return Result.cancelation();
            }
            return Result.failure();
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
            we.cancelMemento();
        }
    }

    private File getScriptFile(String[] script, File directory) {
        File file = new File(directory, "script.tcl");
        FileWriter out;
        try {
            out = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write out conversion script.");
        }
        PrintWriter writer = new PrintWriter(out);
        for (String s : script) {
            writer.println(s);
        }
        writer.close();
        return file;
    }

    private File getInputFile(Petri petri, File directory) {
        final Framework framework = Framework.getInstance();
        StgFormat format = StgFormat.getInstance();
        Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), petri, format);
        if (exporter == null) {
            throw new NoExporterException(petri, format);
        }
        File file = new File(directory, fileName);
        ExportTask exportTask = new ExportTask(exporter, petri, file.getAbsolutePath());
        Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g");
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
    }

    @Override
    public void outputData(byte[] data) {
    }

}
