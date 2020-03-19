package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.converters.FsmToGraphConverter;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.GraphDescriptor;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

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
        final VisualFsm fsm = me.getAs(VisualFsm.class);
        final VisualGraph graph = new VisualGraph(new Graph());
        final FsmToGraphConverter converter = new FsmToGraphConverter(fsm, graph);
        return new ModelEntry(new GraphDescriptor(), converter.getDstModel());
    }

}
