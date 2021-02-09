package org.workcraft.plugins.graph.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.graph.converters.GraphToPetriConverter;
import org.workcraft.plugins.petri.PetriDescriptor;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphToPetriConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, Graph.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        GraphToPetriConverter converter = new GraphToPetriConverter(me.getAs(VisualGraph.class));
        return new ModelEntry(new PetriDescriptor(), converter.getDstModel());
    }

}
