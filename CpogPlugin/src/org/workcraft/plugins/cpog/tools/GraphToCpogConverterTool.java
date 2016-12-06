package org.workcraft.plugins.cpog.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

public class GraphToCpogConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Conditional Partial Order Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, Graph.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final VisualGraph graph = (VisualGraph) me.getVisualModel();
        final VisualCpog cpog = new VisualCpog(new Cpog());
        final GraphToCpogConverter converter = new GraphToCpogConverter(graph, cpog);
        return new ModelEntry(new CpogDescriptor(), converter.getDstModel());
    }

}
