package org.workcraft.plugins.circuit.refinement;

import org.workcraft.Framework;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class RefinementDependencyGraph {

    private final Map<File, Map<String, File>> detailedDependencyMap;

    public RefinementDependencyGraph(WorkspaceEntry we) {
        detailedDependencyMap = new HashMap<>();

        Stack<File> stack = new Stack<>();
        File topFile = Framework.getInstance().getWorkspace().getFile(we);
        Map<String, File> topDependencyMap = extractRefinementMap(we.getModelEntry());
        stack.addAll(topDependencyMap.values());
        detailedDependencyMap.put(topFile, topDependencyMap);

        Set<File> visited = new HashSet<>();
        while (!stack.empty()) {
            File file = stack.pop();
            if ((file != null) && !visited.contains(file)) {
                visited.add(file);
                try {
                    ModelEntry me = WorkUtils.loadModel(file);
                    Map<String, File> dependencyMap = extractRefinementMap(me);
                    stack.addAll(dependencyMap.values());
                    detailedDependencyMap.put(file, dependencyMap);
                } catch (DeserialisationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, File> extractRefinementMap(ModelEntry me) {
        Map<String, File> result = new HashMap<>();
        if (WorkspaceUtils.isApplicable(me, Stg.class)) {
            Stg stg = WorkspaceUtils.getAs(me, Stg.class);
            File refinementFile = stg.getRefinementFile();
            if (refinementFile != null) {
                result.put(null, refinementFile);
            }
        }
        if (WorkspaceUtils.isApplicable(me, Circuit.class)) {
            Circuit circuit = WorkspaceUtils.getAs(me, Circuit.class);
            for (FunctionComponent component : circuit.getFunctionComponents()) {
                File refinementFile = component.getRefinementFile();
                String componentRef = Identifier.truncateNamespaceSeparator(circuit.getNodeReference(component));
                result.put(componentRef, refinementFile);
            }
        }
        return result;
    }

    public Set<File> getVertices() {
        return detailedDependencyMap.keySet();
    }

    public Map<String, File> getDependencyMap(File file) {
        return detailedDependencyMap.getOrDefault(file, Collections.emptyMap());
    }

    public Set<File> getDependencySet(File file) {
        return getDependencyMap(file).values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Map<File, Set<File>> getSimpleGraph() {
        Map<File, Set<File>> result = new HashMap<>();
        for (File file : getVertices()) {
            result.put(file, getDependencySet(file));
        }
        return result;
    }

}
