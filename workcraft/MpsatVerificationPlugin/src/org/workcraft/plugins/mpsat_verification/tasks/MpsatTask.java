package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.traces.Solution;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.TextUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MpsatTask implements Task<MpsatOutput> {

    public static final String OUTPUT_FILE_NAME = "solutions.xml";

    private final File unfoldingFile;
    private final File netFile;
    private final VerificationParameters verificationParameters;
    private final File directory;
    private boolean quiet = false;

    public MpsatTask(File unfoldingFile, File netFile, VerificationParameters verificationParameters, File directory) {
        this.unfoldingFile = unfoldingFile;
        this.netFile = netFile;
        this.verificationParameters = verificationParameters;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
    }

    public void setQuiet(boolean value) {
        quiet = value;
    }

    @Override
    public Result<? extends MpsatOutput> run(ProgressMonitor<? super MpsatOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolPrefix = MpsatVerificationSettings.getCommand();
        String toolSuffix = PunfTask.getToolSuffix(unfoldingFile);
        String toolName = ExecutableUtils.getAbsoluteCommandWithSuffixPath(toolPrefix, toolSuffix);
        command.add(toolName);

        // Built-in arguments
        for (String arg : verificationParameters.getMpsatArguments(directory)) {
            command.add(arg);
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = MpsatVerificationSettings.getArgs();
        if (MpsatVerificationSettings.getAdvancedMode()) {
            String tmp = DialogUtils.showInput("Additional parameters for MPSat:", extraArgs);
            if (tmp == null) {
                return Result.cancel();
            }
            extraArgs = tmp;
        }
        command.addAll(TextUtils.splitWords(extraArgs));

        // Input file
        if (unfoldingFile != null) {
            command.add(unfoldingFile.getAbsolutePath());
        }

        // Output file
        File outputFile = new File(directory, OUTPUT_FILE_NAME);
        command.add(outputFile.getAbsolutePath());

        boolean printStdout = MpsatVerificationSettings.getPrintStdout() && !quiet;
        boolean printStderr = MpsatVerificationSettings.getPrintStderr() && !quiet;
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if (result.isSuccess() && (output != null)) {
            int returnCode = output.getReturnCode();
            if ((returnCode == 0) || (returnCode == 1)) {
                Stg stg = StgUtils.importStg(netFile);
                try {
                    MpsatOutputReader outputReader = new MpsatOutputReader(outputFile);
                    List<Solution> solutions = outputReader.getSolutions();
                    if (outputReader.isSuccess()) {
                        return Result.success(new MpsatOutput(output, stg, solutions, verificationParameters));
                    }
                    return Result.exception(outputReader.getMessage());
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    return Result.exception(e);
                }

            }
            return Result.failure(new MpsatOutput(output, null, null, verificationParameters));
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

}
