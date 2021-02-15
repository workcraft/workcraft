package org.workcraft.plugins.cpog.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.converters.CpogToGraphConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogToGraphConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Directed Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, Cpog.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        CpogToGraphConverter converter = new CpogToGraphConverter(WorkspaceUtils.getAs(me, VisualCpog.class));
        return new ModelEntry(new CpogDescriptor(), converter.getDstModel());
    }

}
