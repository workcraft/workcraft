package org.workcraft.plugins.mpsat_verification.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSubExportOutput extends MultiExportOutput {

    private final List<Map<String, String>> substitutionsList;

    public MultiSubExportOutput(List<File> files, List<Map<String, String>> substitutionsList) {
        super(files);
        this.substitutionsList = substitutionsList;
    }

    public Map<String, String> getSubstitutions(int index) {
        if ((substitutionsList == null) || (index < 0) || (index > substitutionsList.size())) {
            return new HashMap<>();
        }
        return substitutionsList.get(index);
    }

}
