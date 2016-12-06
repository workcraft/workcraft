package org.workcraft.plugins.fsm.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

public class GraphToFsmConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Finite State Machine";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, Graph.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final VisualGraph graph = (VisualGraph) me.getVisualModel();
        final VisualFsm fsm = new VisualFsm(new Fsm());
        final GraphToFsmConverter converter = new GraphToFsmConverter(graph, fsm);
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
