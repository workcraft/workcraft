package org.workcraft.plugins.dfs.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.StgGenerator;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

public class StgGeneratorTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, Dfs.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualDfs dfs = (VisualDfs) me.getVisualModel();
        final StgGenerator generator = new StgGenerator(dfs);
        return new ModelEntry(new StgDescriptor(), generator.getStgModel());
    }

}
