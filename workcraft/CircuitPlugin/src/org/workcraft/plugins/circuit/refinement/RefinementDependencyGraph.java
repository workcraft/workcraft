package org.workcraft.plugins.circuit.refinement;

import org.workcraft.Framework;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class RefinementDependencyGraph {

    private final Map<File, Map<String, File>> detailedDependencyGraph = new HashMap<>();
    private final Map<File, ModelEntry> fileToModelMap = new HashMap<>();

    public RefinementDependencyGraph(WorkspaceEntry we) {
        Stack<File> stack = new Stack<>();
        File topFile = Framework.getInstance().getWorkspace().getFile(we);
        fileToModelMap.put(topFile, we.getModelEntry());
        Map<String, File> topDependencyMap = extractInstanceDependencyMap(we.getModelEntry());
        stack.addAll(topDependencyMap.values());
        detailedDependencyGraph.put(topFile, topDependencyMap);

        Set<File> visited = new HashSet<>();
        while (!stack.empty()) {
            File file = stack.pop();
            if ((file != null) && !visited.contains(file)) {
                visited.add(file);
                try {
                    ModelEntry me = WorkUtils.loadModel(file);
                    fileToModelMap.put(file, me);
                    Map<String, File> dependencyMap = extractInstanceDependencyMap(me);
                    stack.addAll(dependencyMap.values());
                    detailedDependencyGraph.put(file, dependencyMap);
                } catch (DeserialisationException e) {
                    String filePath = FileUtils.getFullPath(file);
                    LogUtils.logError("Cannot read model from file '" + filePath + "':\n" + e.getMessage());
                }
            }
        }
    }

    private Map<String, File> extractInstanceDependencyMap(ModelEntry me) {
        Map<String, File> result = new HashMap<>();
        if (WorkspaceUtils.isApplicable(me, Stg.class)) {
            Stg stg = WorkspaceUtils.getAs(me, Stg.class);
            File refinementFile = stg.getRefinementFile();
            if (refinementFile != null) {
                String label = getInstanceLabel();
                result.put(label, refinementFile);
            }
        }
        if (WorkspaceUtils.isApplicable(me, Circuit.class)) {
            Circuit circuit = WorkspaceUtils.getAs(me, Circuit.class);
            for (FunctionComponent component : circuit.getFunctionComponents()) {
                String label = getInstanceLabel(circuit, component);
                File refinementFile = component.getRefinementFile();
                result.put(label, refinementFile);
            }
        }
        return result;
    }

    public String getInstanceLabel() {
        return "";
    }

    public String getInstanceLabel(Circuit circuit, FunctionComponent component) {
        return Identifier.truncateNamespaceSeparator(circuit.getNodeReference(component))
                + ":" + component.getModule();
    }

    public Set<File> getVertices() {
        return detailedDependencyGraph.keySet();
    }

    public Map<String, File> getInstanceDependencyMap(File file) {
        return detailedDependencyGraph.getOrDefault(file, Collections.emptyMap());
    }

    public Set<File> getDependencies(File file) {
        return getInstanceDependencyMap(file).values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Map<File, Set<File>> getSimpleGraph() {
        Map<File, Set<File>> result = new HashMap<>();
        for (File file : getVertices()) {
            result.put(file, getDependencies(file));
        }
        return result;
    }

    public boolean isStg(File file) {
        ModelEntry me = fileToModelMap.get(file);
        return WorkspaceUtils.isApplicable(me, Stg.class);
    }

    public Set<File> getStgFiles() {
        return getVertices().stream()
                .filter(this::isStg)
                .collect(Collectors.toSet());
    }

    public ModelEntry getModelEntry(File file) {
        return fileToModelMap.get(file);
    }

}
