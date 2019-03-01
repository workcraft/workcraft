package org.workcraft.plugins.plato.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.workcraft.utils.DesktopApi;
import org.workcraft.plugins.plato.PlatoSettings;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;

public class PlatoTask implements Task<ExternalProcessOutput> {

    private final File inputFile;
    private final Object[] includeList;
    private final boolean fst;
    private final boolean system;

    public PlatoTask(File inputFile) {
        this.inputFile = inputFile;
        this.includeList = new String[0];
        this.fst = false;
        this.system = false;
    }

    public PlatoTask(File inputFile, Object[] includeList, boolean fst, boolean system) {
        this.inputFile = inputFile;
        this.includeList = includeList;
        this.fst = fst;
        this.system = system;
    }

    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {
        try {
            ArrayList<String> command = buildCommand();

            ExternalProcessTask task = new ExternalProcessTask(command, new File("."));
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessOutput> result = task.run(mon);
            if (result.getOutcome() != Outcome.SUCCESS) {
                return result;
            }
            ExternalProcessOutput output = result.getPayload();
            if (output.getReturnCode() == 0) {
                return Result.success(output);
            } else {
                return Result.failure(output);
            }
        } catch (NullPointerException e) {
            // Open window dialog was cancelled, do nothing
        }
        return null;
    }

    private ArrayList<String> buildCommand() {
        ArrayList<String> command = new ArrayList<>();

        command.add("stack");
        String translateLocation = "";
        if (system) {
            command.add("ghc");
            translateLocation = DesktopApi.getOs().isWindows() ? "translate\\System.hs" : "translate/System.hs";
        } else {
            command.add("runghc");
            translateLocation = DesktopApi.getOs().isWindows() ? "translate\\Component.hs" : "translate/Component.hs";
        }
        command.add(PlatoSettings.getPlatoFolderLocation() + translateLocation);
        String stackYamlCommand = "--stack-yaml=" + PlatoSettings.getPlatoFolderLocation() + "stack.yaml";
        command.add(stackYamlCommand);
        command.add("--");
        if (system) {
            command.add("-i");
        }
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

        if (system) {
            File hidir = new File(System.getProperty("java.io.tmpdir", null), "hidir");
            if (!hidir.exists() && !hidir.mkdir()) {
                System.out.println("Error creating temp file for compilation");
            }
            command.add("-hidir " + hidir.getAbsolutePath());
        }

        return command;
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
