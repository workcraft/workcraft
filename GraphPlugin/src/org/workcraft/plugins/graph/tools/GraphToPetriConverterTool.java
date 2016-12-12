package org.workcraft.plugins.graph.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

public class GraphToPetriConverterTool extends ConversionTool {

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
