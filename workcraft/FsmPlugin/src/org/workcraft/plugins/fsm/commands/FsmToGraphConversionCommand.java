package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.converters.FsmToGraphConverter;
import org.workcraft.plugins.graph.GraphDescriptor;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class FsmToGraphConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Directed Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualFsm.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        FsmToGraphConverter converter = new FsmToGraphConverter(me.getAs(VisualFsm.class));
        return new ModelEntry(new GraphDescriptor(), converter.getDstModel());
    }

}
