package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.tasks.*;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.TraceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MpsatLtlxTask implements Task<MpsatOutput> {

    private static final Pattern SOLUTION_PATTERN = Pattern.compile(
            "The property is violated:\\s*\\R\\s*Prefix: (.+)\\R\\s*Loop: (.+)",
            Pattern.UNIX_LINES);

    private final File netFile;
    private final File hoaFile;
    private final File directory;

    public MpsatLtlxTask(File netFile, File hoaFile, File directory) {
        this.netFile = netFile;
        this.hoaFile = hoaFile;
        this.directory = directory;
    }

    @Override
    public Result<? extends MpsatOutput> run(ProgressMonitor<? super MpsatOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(MpsatVerificationSettings.getCommand());
        command.add(toolName);

        // Extra arguments (should go before the file parameters)
        command.addAll(TextUtils.splitWords(MpsatVerificationSettings.getArgs()));

        // Built-in arguments
        command.add("-B=" + hoaFile.getAbsolutePath());
        command.add("-c");
        command.add(netFile.getAbsolutePath());

        boolean printStdout = MpsatVerificationSettings.getPrintStdout();
        boolean printStderr = MpsatVerificationSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if (result.isSuccess() && (output != null)) {
            List<Solution> solutions = getSolutions(output.getStdoutString());
            MpsatOutput mpsatOutput = new MpsatOutput(output, null, netFile, null, solutions);
            int returnCode = output.getReturnCode();
            if ((returnCode == 0) || (returnCode == 1)) {
                return Result.success(mpsatOutput);
            }
            return Result.failure(mpsatOutput);
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

    private List<Solution> getSolutions(String text) {
        Solution solution = getSolution(text);
        return solution == null ? Collections.emptyList() : Collections.singletonList(solution);
    }

    private Solution getSolution(String text) {
        Matcher matcher = SOLUTION_PATTERN.matcher(text);
        if (matcher.find()) {
            Trace trace = TraceUtils.deserialiseTrace(matcher.group(1));
            int loopPosition = trace.size();
            trace.addAll(TraceUtils.deserialiseTrace(matcher.group(2)));
            Solution solution = new Solution(trace);
            solution.setLoopPosition(loopPosition);
            return solution;
        }
        return null;
    }

}
