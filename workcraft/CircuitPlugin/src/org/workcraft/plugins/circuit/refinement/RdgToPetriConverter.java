package org.workcraft.plugins.circuit.refinement;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class RdgToPetriConverter {

    private final RefinementDependencyGraph rdg;
    private final VisualPetri petri;
    private final Map<File, VisualTransition> vertexToTransitionMap;

    public RdgToPetriConverter(RefinementDependencyGraph rdg) {
        this.rdg = rdg;
        petri = new VisualPetri(new Petri());
        vertexToTransitionMap = convertVerticesToTransitions();
        convertDependenciesToPlaces();
        createInitialPlace();
    }

    private Map<File, VisualTransition> convertVerticesToTransitions() {
        Map<File, VisualTransition> result = new HashMap<>();
        for (File vertex : rdg.getVertices()) {
            VisualTransition transition = petri.createTransition(null, null);
            result.put(vertex, transition);
        }
        return result;
    }

    private void convertDependenciesToPlaces() {
        for (File fromVertex : rdg.getVertices()) {
            petri.selectNone();
            VisualTransition fromTransition = vertexToTransitionMap.get(fromVertex);
            petri.addToSelection(fromTransition);

            Map<String, File> dependencyMap = rdg.getDependencyMap(fromVertex);
            for (String throughLabel : dependencyMap.keySet()) {
                VisualPlace throughPlace = petri.createPlace(null, null);
                throughPlace.setLabel(throughLabel);
                petri.addToSelection(throughPlace);
                connectIfPossible(petri, fromTransition, throughPlace);

                File toVertex = dependencyMap.get(throughLabel);
                if (toVertex != null) {
                    VisualTransition toTransition = vertexToTransitionMap.get(toVertex);
                    connectIfPossible(petri, throughPlace, toTransition);
                }
            }
            VisualPage page = petri.groupPageSelection();
            page.setLabel(fromVertex.getName());
        }
    }

    private VisualPlace createInitialPlace() {
        VisualPlace place = petri.createPlace(null, null);
        place.getReferencedComponent().setTokens(1);
        for (VisualTransition transition : petri.getVisualTransitions()) {
            if (petri.getPreset(transition).isEmpty()) {
                connectIfPossible(petri, place, transition);
            }
        }
        return place;
    }

    public RefinementDependencyGraph getRdg() {
        return rdg;
    }

    public VisualPetri getPetri() {
        return petri;
    }

    private void connectIfPossible(VisualModel model, VisualNode fromNode, VisualNode toNode) {
        if ((fromNode != null) && (toNode != null)) {
            try {
                model.connect(fromNode, toNode);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
    }

    public void highlightCycle(List<File> cycle, Color color) {
        if ((cycle == null) || cycle.isEmpty()) {
            return;
        }
        VisualTransition predTransition = vertexToTransitionMap.get(cycle.get(cycle.size() - 1));
        for (File vertex : cycle) {
            VisualTransition transition = vertexToTransitionMap.get(vertex);
            if (transition != null) {
                transition.setForegroundColor(color);
                if (predTransition != null) {
                    Set<VisualNode> nodes = new HashSet<>(petri.getPreset(transition));
                    nodes.retainAll(petri.getPostset(predTransition));
                    highlightNodesAndAdjacentConnections(nodes, color);
                }
                predTransition = transition;
            }
        }
    }

    private void highlightNodesAndAdjacentConnections(Collection<VisualNode> nodes, Color color) {
        for (VisualNode node : nodes) {
            if (node instanceof VisualPlace) {
                VisualPlace place = (VisualPlace) node;
                place.setForegroundColor(color);
            }
            for (VisualConnection connection : petri.getConnections(node)) {
                connection.setColor(color);
            }
        }
    }

}
