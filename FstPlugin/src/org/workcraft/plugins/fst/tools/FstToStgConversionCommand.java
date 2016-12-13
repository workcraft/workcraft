package org.workcraft.plugins.fst.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class FstToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, VisualFst.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualFst fst = WorkspaceUtils.getAs(me, VisualFst.class);
        final VisualStg stg = new VisualStg(new Stg());
        final FstToStgConverter converter = new FstToStgConverter(fst, stg);
        return new ModelEntry(new StgDescriptor(), converter.getDstModel());
    }

}
