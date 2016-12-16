package org.workcraft.plugins.fsm.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
        final VisualGraph graph = me.getAs(VisualGraph.class);
        final VisualFsm fsm = new VisualFsm(new Fsm());
        final GraphToFsmConverter converter = new GraphToFsmConverter(graph, fsm);
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
