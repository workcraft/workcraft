package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.tasks.ExportOutput;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SubExportOutput extends ExportOutput {

    private final Map<String, String> substitutions;

    public SubExportOutput(File file, Map<String, String> substitutions) {
        super(file);
        this.substitutions = substitutions;
    }

    public Map<String, String> getSubstitutions() {
        return substitutions == null ? new HashMap<>() : substitutions;
    }

}
