package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.tasks.ExportOutput;

import java.io.File;

public class CompositionExportOutput extends ExportOutput {

    private final CompositionData compositionData;

    public CompositionExportOutput(File file, CompositionData compositionData) {
        super(file);
        this.compositionData = compositionData;
    }

    public CompositionData getCompositionData() {
        return compositionData;
    }

}
