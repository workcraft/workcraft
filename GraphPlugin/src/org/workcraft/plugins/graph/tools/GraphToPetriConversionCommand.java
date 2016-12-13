package org.workcraft.plugins.graph.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class GraphToPetriConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, Graph.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualGraph graph = (VisualGraph) me.getVisualModel();
        final VisualPetriNet petri = new VisualPetriNet(new PetriNet());
        final GraphToPetriConverter converter = new GraphToPetriConverter(graph, petri);
        return new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
    }

}
