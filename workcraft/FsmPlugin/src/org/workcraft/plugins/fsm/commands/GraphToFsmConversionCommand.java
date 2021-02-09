package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.converters.GraphToFsmConverter;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphToFsmConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Finite State Machine";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualGraph.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        GraphToFsmConverter converter = new GraphToFsmConverter(me.getAs(VisualGraph.class));
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
