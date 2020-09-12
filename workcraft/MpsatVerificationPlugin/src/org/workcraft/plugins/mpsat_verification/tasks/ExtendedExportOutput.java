package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.tasks.ExportOutput;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExtendedExportOutput extends ExportOutput {

    private final Map<File, Map<String, String>> fileToSubstitutionsMap = new HashMap<>();

    public ExtendedExportOutput() {
        super(null);
    }

    public void add(File file, Map<String, String> substitutions) {
        fileToSubstitutionsMap.put(file, substitutions);
    }

    public Collection<File> getFiles() {
        return Collections.unmodifiableCollection(fileToSubstitutionsMap.keySet());
    }

    public Map<String, String> getSubstitutions(File file) {
        return Collections.unmodifiableMap(fileToSubstitutionsMap.getOrDefault(file, Collections.emptyMap()));
    }

}
