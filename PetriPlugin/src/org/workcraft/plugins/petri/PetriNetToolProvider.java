package org.workcraft.plugins.petri;

import java.util.ArrayList;

import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.petri.tools.PetriConnectionTool;
import org.workcraft.plugins.petri.tools.PetriPlaceGeneratorTool;
import org.workcraft.plugins.petri.tools.PetriSelectionTool;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;
import org.workcraft.plugins.petri.tools.PetriTransitionGeneratorTool;
import org.workcraft.plugins.petri.tools.ReadArcConnectionTool;

public class PetriNetToolProvider implements CustomToolsProvider {

    @Override
    public Iterable<GraphEditorTool> getTools() {
        ArrayList<GraphEditorTool> result = new ArrayList<>();

        result.add(new PetriSelectionTool());
        result.add(new CommentGeneratorTool());
        result.add(new PetriConnectionTool());
        result.add(new ReadArcConnectionTool());
        result.add(new PetriPlaceGeneratorTool());
        result.add(new PetriTransitionGeneratorTool());
        result.add(new PetriSimulationTool());

        return result;
    }

}
