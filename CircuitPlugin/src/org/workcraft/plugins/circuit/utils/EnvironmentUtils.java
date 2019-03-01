package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Environment;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class EnvironmentUtils {

    private static WorkspaceEntry getWorkspaceEntry(Circuit circuit) {
        Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();
        for (WorkspaceEntry we: workspace.getWorks()) {
            ModelEntry me = we.getModelEntry();
            if (me.getMathModel() == circuit) {
                return we;
            }
        }
        return null;
    }

    public static File getEnvironmentFile(Circuit circuit) {
        File file = null;
        for (Environment environment : Hierarchy.getChildrenOfType(circuit.getRoot(), Environment.class)) {
            file = environment.getFile();
            File base = environment.getBase();
            if (base != null) {
                String basePath = base.getPath().replace("\\", "/");
                String filePath = file.getPath().replace("\\", "/");
                if (!basePath.isEmpty() && filePath.startsWith(basePath)) {
                    WorkspaceEntry we = getWorkspaceEntry(circuit);
                    File newBase = we == null ? null : we.getFile().getParentFile();
                    if (newBase != null) {
                        String relativePath = filePath.substring(basePath.length());
                        while (relativePath.startsWith("/")) {
                            relativePath = relativePath.substring(1);
                        }
                        file = new File(newBase, relativePath);
                    }
                }
            }
            break;
        }
        return file;
    }

    public static void setEnvironmentFile(Circuit circuit, File file) {
        File oldFile = getEnvironmentFile(circuit);
        boolean envChanged = ((oldFile == null) && (file != null)) || ((oldFile != null) && !oldFile.equals(file));
        WorkspaceEntry we = getWorkspaceEntry(circuit);
        if (envChanged && (we != null)) {
            we.saveMemento();
            we.setChanged(true);
            File base = we.getFile().getParentFile();
            setEnvironment(circuit, file, base);
        }
    }

    public static void updateEnvironmentFile(Circuit circuit) {
        File file = getEnvironmentFile(circuit);
        WorkspaceEntry we = getWorkspaceEntry(circuit);
        File base = (we == null) ? null : we.getFile().getParentFile();
        setEnvironment(circuit, file, base);
    }

    private static void setEnvironment(Circuit circuit, File file, File base) {
        for (Environment environment : Hierarchy.getChildrenOfType(circuit.getRoot(), Environment.class)) {
            circuit.remove(environment);
        }
        if (file != null) {
            Environment env = new Environment();
            env.setFile(file);
            env.setBase(base);
            circuit.add(env);
        }
    }

}
