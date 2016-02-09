package org.workcraft.workspace;

import java.util.List;

import org.workcraft.gui.workspace.Path;
import org.workcraft.util.ListMap;

public class DependencyManager {
    private ListMap<Path<String>, Path<String>> associations;

    public void createAssociation(Path<String> dependentFile, Path<String> masterFile) {
        associations.put(masterFile, dependentFile);
    }

    public List<Path<String>> getAssociatedFiles(Path<String> masterFile) {
        return associations.get(masterFile);
    }
}
