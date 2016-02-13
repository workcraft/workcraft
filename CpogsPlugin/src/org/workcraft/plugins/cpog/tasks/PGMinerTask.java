package org.workcraft.plugins.cpog.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;

public class PGMinerTask implements Task<ExternalProcessResult> {

    private File inputFile;
    private boolean split;

    public PGMinerTask(File inputFile, boolean split) {
        this.inputFile = inputFile;
        this.split = split;
    }

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        //Build the commands for PGMiner
        try {
            ArrayList<String> command = new ArrayList<>();
            String toolName = ToolUtils.getAbsoluteCommandPath(CpogSettings.getPgminerCommand());
            command.add(toolName);

            if (split) {
                command.add("-split");
            }
            command.add(inputFile.getAbsolutePath());

            //Call PGMiner
            ExternalProcessTask task = new ExternalProcessTask(command, new File("."));
            SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);

            Result<? extends ExternalProcessResult> result = task.run(mon);

            if (result.getOutcome() != Outcome.FINISHED) {

                return result;
            }
            Map<String, byte[]> outputFiles = new HashMap<String, byte[]>();
            try {
                File outputFile = getOutputFile(inputFile);
                if (outputFile.exists()) {
                    outputFiles.put("output.cpog", FileUtils.readAllBytes(outputFile));
                }
            } catch (IOException e) {
                return new Result<ExternalProcessResult>(e);
            }

            ExternalProcessResult retVal = result.getReturnValue();
            ExternalProcessResult finalResult = new ExternalProcessResult(retVal.getReturnCode(), retVal.getOutput(), retVal.getErrors(), outputFiles);
            if (retVal.getReturnCode() == 0) {
                return Result.finished(finalResult);
            } else {
                return Result.failed(finalResult);
            }
        } catch (NullPointerException e) {
            //Open window dialog was cancelled, do nothing
        }

        return null;
    }

    public File getOutputFile(File inputFile) {

        String filePath = inputFile.getAbsolutePath();

        int index = filePath.lastIndexOf('/');
        String fileName = filePath.substring(index + 1);
        String suffix = fileName.substring(fileName.indexOf('.'));
        fileName = fileName.replace(suffix, "") + ".cpog";
        filePath = filePath.substring(0, index + 1);
        File outputFile = new File(filePath + fileName);

        return outputFile;
    }

}
