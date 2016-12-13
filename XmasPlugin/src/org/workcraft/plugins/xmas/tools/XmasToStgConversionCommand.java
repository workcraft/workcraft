package org.workcraft.plugins.xmas.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.stg.XmasToStgConverter;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class XmasToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, Xmas.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualXmas xmas = (VisualXmas) me.getVisualModel();
        final XmasToStgConverter converter = new XmasToStgConverter(xmas);
        return new ModelEntry(new StgDescriptor(), converter.getStgModel());
    }

}
