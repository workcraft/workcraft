package org.workcraft.plugins.xmas.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.stg.XmasToStgConverter;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class XmasToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, Xmas.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualXmas xmas = me.getAs(VisualXmas.class);
        final XmasToStgConverter converter = new XmasToStgConverter(xmas);
        return new ModelEntry(new StgDescriptor(), converter.getStgModel());
    }

}
