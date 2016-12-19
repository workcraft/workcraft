package org.workcraft.plugins.petri;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.tools.PetriConnectionTool;
import org.workcraft.plugins.petri.tools.PetriSelectionTool;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;
import org.workcraft.plugins.petri.tools.ReadArcConnectionTool;

public class PetriNetToolProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new PetriSelectionTool());
        result.add(new CommentGeneratorTool());
        result.add(new PetriConnectionTool());
        result.add(new ReadArcConnectionTool());
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Place.class)));
        result.add(new NodeGeneratorTool(new DefaultNodeGenerator(Transition.class)));
        result.add(new PetriSimulationTool());

        return result;
    }

}
