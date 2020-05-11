package org.workcraft.plugins.punf.tasks;

import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.tasks.*;
import org.workcraft.utils.ExecutableUtils;

import java.io.File;
import java.util.ArrayList;

public class Ltl2tgbaTask implements Task<Ltl2tgbaOutput> {

    private static final String OUTPUT_FILE_NAME = "assertion.hoa";

    private final File inputFile;
    private final File directory;

    public Ltl2tgbaTask(File inputFile, File directory) {
        this.inputFile = inputFile;
        this.directory = directory;
    }

    @Override
    public Result<? extends Ltl2tgbaOutput> run(ProgressMonitor<? super Ltl2tgbaOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(PunfSettings.getLtl2tgbaCommand());
        command.add(toolName);

        // Built-in arguments
        command.add("-B");
        command.add("--negate");
        command.add("--check=stutter-invariant");
        command.add("--check=stutter-sensitive-example");
        command.add("-F");
        command.add(inputFile.getAbsolutePath());

        File outputFile = new File(directory, OUTPUT_FILE_NAME);
        command.add("-o");
        command.add(outputFile.getAbsolutePath());
        outputFile.deleteOnExit();

        boolean printStdout = PunfSettings.getPrintStdout();
        boolean printStderr = PunfSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if (result.isSuccess() && (output != null)) {
            Ltl2tgbaOutput ltl2tgbaOutput = new Ltl2tgbaOutput(output, inputFile, outputFile);
            int returnCode = output.getReturnCode();
            if ((returnCode == 0) || (returnCode == 1)) {
                return Result.success(ltl2tgbaOutput);
            }
            return Result.failure(ltl2tgbaOutput);
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

}
