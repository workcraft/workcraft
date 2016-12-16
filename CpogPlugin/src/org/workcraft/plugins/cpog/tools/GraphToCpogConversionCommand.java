package org.workcraft.plugins.cpog.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class GraphToCpogConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Conditional Partial Order Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, Graph.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualGraph graph = me.getAs(VisualGraph.class);
        final VisualCpog cpog = new VisualCpog(new Cpog());
        final GraphToCpogConverter converter = new GraphToCpogConverter(graph, cpog);
        return new ModelEntry(new CpogDescriptor(), converter.getDstModel());
    }

}
