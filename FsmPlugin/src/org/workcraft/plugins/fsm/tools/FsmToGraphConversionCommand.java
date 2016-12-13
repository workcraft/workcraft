package org.workcraft.plugins.fsm.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.GraphDescriptor;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class FsmToGraphConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Directed Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, VisualFsm.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualFsm fsm = WorkspaceUtils.getAs(me, VisualFsm.class);
        final VisualGraph graph = new VisualGraph(new Graph());
        final FsmToGraphConverter converter = new FsmToGraphConverter(fsm, graph);
        return new ModelEntry(new GraphDescriptor(), converter.getDstModel());
    }

}
