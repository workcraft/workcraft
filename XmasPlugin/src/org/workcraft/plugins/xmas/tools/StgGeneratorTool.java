package org.workcraft.plugins.xmas.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.stg.StgGenerator;
import org.workcraft.workspace.ModelEntry;

public class StgGeneratorTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof Xmas;
    }

    @Override
    public ModelEntry apply(ModelEntry me) {
        final VisualXmas xmas = (VisualXmas) me.getVisualModel();
        final StgGenerator generator = new StgGenerator(xmas);
        return new ModelEntry(new StgDescriptor(), generator.getStgModel());
    }

}
