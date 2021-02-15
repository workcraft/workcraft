package org.workcraft.plugins.cpog.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.converters.GraphToCpogConverter;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

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
        GraphToCpogConverter converter = new GraphToCpogConverter(me.getAs(VisualGraph.class));
        return new ModelEntry(new CpogDescriptor(), converter.getDstModel());
    }

}
