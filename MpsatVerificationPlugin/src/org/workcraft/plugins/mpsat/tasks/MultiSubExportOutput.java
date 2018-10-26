package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSubExportOutput extends MultiExportOutput {

    private final List<Map<String, String>> substitutions;

    public MultiSubExportOutput(List<File> files, List<Map<String, String>> substitutions) {
        super(files);
        this.substitutions = substitutions;
    }

    public Map<String, String> getSubstitutions(int index) {
        if ((substitutions == null) || (index < 0) || (index > substitutions.size())) {
            return new HashMap<>();
        }
        return substitutions.get(index);
    }

}
