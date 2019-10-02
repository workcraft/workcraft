package org.workcraft.plugins.xbm.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.xbm.VisualXbm;
import org.workcraft.plugins.xbm.Xbm;
import org.workcraft.plugins.xbm.converters.XbmToStgConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class XbmToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "STG";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xbm.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        VisualXbm visualXbm = WorkspaceUtils.getAs(me, VisualXbm.class);
        VisualStg visualStg = new VisualStg(new Stg());
        XbmToStgConverter converter = new XbmToStgConverter(visualXbm, visualStg);
        return new ModelEntry(new StgDescriptor(), converter.getDstModel());
    }
}
