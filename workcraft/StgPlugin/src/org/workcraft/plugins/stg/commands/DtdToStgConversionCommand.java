package org.workcraft.plugins.stg.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.dtd.Dtd;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.converters.DtdToStgConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class DtdToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Dtd.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        for (WorkspaceEntry we: workspace.getWorks()) {
            if (we.getModelEntry() != me) continue;
            we.captureMemento();
            try {
                final VisualDtd dtd = me.getAs(VisualDtd.class);
                final VisualStg stg = new VisualStg(new Stg());
                final DtdToStgConverter converter = new DtdToStgConverter(dtd, stg);
                return new ModelEntry(new StgDescriptor(), converter.getDstModel());
            } finally {
                we.cancelMemento();
            }
        }
        return null;
    }

}
