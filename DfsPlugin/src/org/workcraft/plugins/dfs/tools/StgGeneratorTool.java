package org.workcraft.plugins.dfs.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.StgGenerator;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.workspace.ModelEntry;

public class StgGeneratorTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof Dfs;
    }

    @Override
    public ModelEntry apply(ModelEntry me) {
        final VisualDfs dfs = (VisualDfs) me.getVisualModel();
        final StgGenerator generator = new StgGenerator(dfs);
        return new ModelEntry(new StgDescriptor(), generator.getStgModel());
    }

}
