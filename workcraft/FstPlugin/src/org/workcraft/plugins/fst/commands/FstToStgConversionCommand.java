package org.workcraft.plugins.fst.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToStgConverter;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

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
        FstToStgConverter converter = new FstToStgConverter(me.getAs(VisualFst.class));
        return new ModelEntry(new StgDescriptor(), converter.getDstModel());
    }

}
