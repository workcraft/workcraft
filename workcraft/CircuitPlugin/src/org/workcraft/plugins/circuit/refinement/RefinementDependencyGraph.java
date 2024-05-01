package org.workcraft.plugins.circuit.refinement;

import org.workcraft.Framework;
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

    private static final String COMPOSITE_LABEL_SEPARATOR = ":";

    private final Map<File, Map<String, File>> detailedDependencyGraph = new HashMap<>();
    private final Map<File, ModelEntry> fileToModelMap = new HashMap<>();

    public RefinementDependencyGraph(WorkspaceEntry we) {
        this(Framework.getInstance().getWorkspace().getFile(we));
    }

    public RefinementDependencyGraph(File topFile) {
        Stack<File> stack = new Stack<>();
        try {
            ModelEntry topMe = WorkUtils.loadModel(topFile);
            fileToModelMap.put(topFile, topMe);
            Map<String, File> topDependencyMap = extractInstanceDependencyMap(topMe);
            stack.addAll(topDependencyMap.values());
            detailedDependencyGraph.put(topFile, topDependencyMap);
        } catch (DeserialisationException e) {
            String filePath = FileUtils.getFullPath(topFile);
            LogUtils.logError("Cannot read top-level file '" + filePath + "':\n" + e.getMessage());
        }
        Set<File> visited = new HashSet<>();
        while (!stack.empty()) {
            File file = stack.pop();
            if ((file != null) && !visited.contains(file)) {
                visited.add(file);
                if (!FileUtils.isReadableFile(file)) {
                    fileToModelMap.put(file, null);
                    detailedDependencyGraph.put(file, Collections.emptyMap());
                } else {
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
    }

    private Map<String, File> extractInstanceDependencyMap(ModelEntry me) {
        Map<String, File> result = new HashMap<>();
        if (WorkspaceUtils.isApplicable(me, Stg.class)) {
            Stg stg = WorkspaceUtils.getAs(me, Stg.class);
            File refinementFile = stg.getRefinementFile();
            if (refinementFile != null) {
                String label = createEmptyLabel();
                result.put(label, refinementFile);
            }
        }
        if (WorkspaceUtils.isApplicable(me, Circuit.class)) {
            Circuit circuit = WorkspaceUtils.getAs(me, Circuit.class);
            for (FunctionComponent component : circuit.getFunctionComponents()) {
                String label = createCompositeLabel(circuit, component);
                File refinementFile = component.getRefinementFile();
                result.put(label, refinementFile);
            }
        }
        return result;
    }

    public static String createEmptyLabel() {
        return "";
    }

    public static String createCompositeLabel(Circuit circuit, FunctionComponent component) {
        return circuit.getComponentReference(component) + COMPOSITE_LABEL_SEPARATOR + component.getModule();
    }

    public static boolean isCompositeLabel(String label) {
        return (label != null) && label.contains(COMPOSITE_LABEL_SEPARATOR);
    }

    public static String getCompositeLabelPrefix(String compositeLabel) {
        int i = compositeLabel.indexOf(COMPOSITE_LABEL_SEPARATOR);
        return i < 0 ? compositeLabel : compositeLabel.substring(0, i);
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

    public boolean isCircuit(File file) {
        ModelEntry me = fileToModelMap.get(file);
        return WorkspaceUtils.isApplicable(me, Circuit.class);
    }

    public boolean isStg(File file) {
        ModelEntry me = fileToModelMap.get(file);
        return WorkspaceUtils.isApplicable(me, Stg.class);
    }

    public Set<File> getCircuitFiles() {
        return getVertices().stream()
                .filter(this::isCircuit)
                .collect(Collectors.toSet());
    }

    public Set<File> getStgFiles() {
        return getVertices().stream()
                .filter(this::isStg)
                .collect(Collectors.toSet());
    }

    public Set<File> getInvalidFiles() {
        return getVertices().stream()
                .filter(file -> !isStg(file) && !isCircuit(file))
                .collect(Collectors.toSet());
    }

    public ModelEntry getModelEntry(File file) {
        return fileToModelMap.get(file);
    }

}
