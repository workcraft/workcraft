package org.workcraft.plugins.fst.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToStgConverter;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class FstToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualFst.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualFst fst = me.getAs(VisualFst.class);
        final VisualStg stg = new VisualStg(new Stg());
        final FstToStgConverter converter = new FstToStgConverter(fst, stg);
        return new ModelEntry(new StgDescriptor(), converter.getDstModel());
    }

}
