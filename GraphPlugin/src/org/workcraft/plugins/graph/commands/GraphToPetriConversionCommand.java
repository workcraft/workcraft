package org.workcraft.plugins.graph.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.graph.converters.GraphToPetriConverter;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

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
        final VisualGraph graph = me.getAs(VisualGraph.class);
        final VisualPetriNet petri = new VisualPetriNet(new PetriNet());
        final GraphToPetriConverter converter = new GraphToPetriConverter(graph, petri);
        return new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
    }

}
