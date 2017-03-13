package org.workcraft.plugins.plato.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.plato.PlatoSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;

public class PlatoTask implements Task<ExternalProcessResult> {

    private final File inputFile;
    private final Object[] includeList;
    private final boolean fst;

    public PlatoTask(File inputFile) {
        this.inputFile = inputFile;
        this.includeList = new String[0];
        this.fst = false;
    }

    public PlatoTask(File inputFile, Object[] includeList, boolean fst) {
        this.inputFile = inputFile;
        this.includeList = includeList;
        this.fst = fst;
    }

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        try {
            ArrayList<String> command = new ArrayList<>();

            command.add("stack");
            command.add("runghc");
            String translateLocation = DesktopApi.getOs().isWindows() ? "translate\\Main.hs" : "translate/Main.hs";
            command.add(PlatoSettings.getPlatoFolderLocation() + translateLocation);
            String stackYamlCommand = "--stack-yaml=" + PlatoSettings.getPlatoFolderLocation() + "stack.yaml";
            command.add(stackYamlCommand);
            command.add("--");
            command.add(inputFile.getAbsolutePath());

            if (fst) {
                command.add("-f");
            }

            Object[] includeSetting = getIncludedSetting();
            Object[] allIncludes = concatIncludes(includeSetting, includeList);

            for (Object i : allIncludes) {
                File f = new File((String) i);
                if (!f.isDirectory()) {
                    command.add("-i");
                    command.add(i.toString());
                } else {
                    includeFilesInDirectory(f, command);
                }
            }

            ExternalProcessTask task = new ExternalProcessTask(command, new File("."));
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessResult> result = task.run(mon);
            if (result.getOutcome() != Outcome.FINISHED) {
                return result;
            }
            ExternalProcessResult retVal = result.getReturnValue();
            ExternalProcessResult finalResult = new ExternalProcessResult(retVal.getReturnCode(), retVal.getOutput(),
                    retVal.getErrors(), null);

            if (retVal.getReturnCode() == 0) {
                return Result.finished(finalResult);
            } else {
                return Result.failed(finalResult);
            }
        } catch (NullPointerException e) {
            // Open window dialog was cancelled, do nothing
        }
        return null;
    }

    private Object[] concatIncludes(Object[] a, Object[] b) {
        ArrayList<Object> result = new ArrayList<>();
        Collections.addAll(result, a);
        Collections.addAll(result, b);
        return result.toArray(new Object[result.size()]);
    }

    private Object[] getIncludedSetting() {
        Object[] list = PlatoSettings.getPlatoIncludesList().split(";");
        if ((list.length == 1) && (list[0].toString() == "")) {
            return new Object[0];
        }
        return list;
    }

    private void includeFilesInDirectory(File directory, ArrayList<String> command) {
        for (File f : directory.listFiles()) {
            String path = f.getAbsolutePath();
            if (!f.isDirectory()) {
                int k = path.lastIndexOf('.');
                if (k != -1) {
                    String ex = path.substring(k);
                    if (ex.compareTo(".hs") == 0) {
                        command.add("-i");
                        command.add(path);
                    }
                }
            }
        }
    }

}
