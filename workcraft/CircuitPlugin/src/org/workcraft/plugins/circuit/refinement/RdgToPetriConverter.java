package org.workcraft.plugins.circuit.refinement;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

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
            VisualTransition fromTransition = getVertexTransition(fromVertex);
            fromTransition.setNamePositioning(Positioning.CENTER);
            petri.addToSelection(fromTransition);

            Map<String, File> dependencyMap = rdg.getInstanceDependencyMap(fromVertex);
            for (String ref : dependencyMap.keySet()) {
                VisualPlace throughPlace = petri.createPlace(null, null);
                petri.addToSelection(throughPlace);
                connectIfPossible(petri, fromTransition, throughPlace);

                File toVertex = dependencyMap.get(ref);
                if (toVertex != null) {
                    VisualTransition toTransition = getVertexTransition(toVertex);
                    connectIfPossible(petri, throughPlace, toTransition);
                } else if (ref.contains(":")) {
                    throughPlace.setFillColor(AnalysisDecorationSettings.getDontTouchColor());
                }

                throughPlace.setLabel(ref);
                throughPlace.setLabelPositioning(Positioning.BOTTOM);
                throughPlace.setNamePositioning(Positioning.CENTER);
            }
            VisualPage page = petri.groupPageSelection();
            page.setLabel(fromVertex.getName());
            page.setLabelPositioning(Positioning.TOP);
            page.setNamePositioning(Positioning.CENTER);
        }
    }

    private void createInitialPlace() {
        Collection<VisualTransition> topTransitions = petri.getVisualTransitions().stream()
                .filter(transition -> petri.getPreset(transition).isEmpty())
                .collect(Collectors.toSet());

        if (!topTransitions.isEmpty()) {
            VisualPlace place = petri.createPlace(null, null);
            place.getReferencedComponent().setTokens(1);
            place.setNamePositioning(Positioning.CENTER);
            topTransitions.forEach(transition -> connectIfPossible(petri, place, transition));
        }
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
            }
        }
    }

    public void highlightCycle(List<File> cycle, Color color) {
        if ((cycle == null) || cycle.isEmpty()) {
            return;
        }
        VisualTransition predTransition = getVertexTransition(cycle.get(cycle.size() - 1));
        for (File vertex : cycle) {
            VisualTransition transition = getVertexTransition(vertex);
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

    public void highlightInstances(Map<File, Set<String>> vertices, Color color) {
        if ((vertices == null) || vertices.isEmpty()) {
            return;
        }
        for (File vertex : vertices.keySet()) {
            Set<String> labels = vertices.get(vertex);
            VisualPage page = getVertexPage(vertex);
            if (page != null) {
                for (VisualPlace place : getVertexPlaces(vertex)) {
                    if (labels.contains(place.getLabel())) {
                        place.setFillColor(color);
                        page.setFillColor(color);
                    }
                }
            }
        }
    }

    private VisualTransition getVertexTransition(File file) {
        return vertexToTransitionMap.get(file);
    }

    private VisualPage getVertexPage(File vertex) {
        VisualTransition transition = getVertexTransition(vertex);
        if (transition != null) {
            Node parent = transition.getParent();
            if (parent instanceof VisualPage) {
                return (VisualPage) parent;
            }
        }
        return null;
    }

    private Set<VisualPlace> getVertexPlaces(File vertex) {
        Set<VisualPlace> result = new HashSet<>();
        VisualPage page = getVertexPage(vertex);
        if (page != null) {
            result.addAll(Hierarchy.getDescendantsOfType(page, VisualPlace.class));
        }
        return result;
    }

    public void collapse(Collection<File> vertices) {
        vertices.stream()
                .map(this::getVertexPage)
                .filter(Objects::nonNull)
                .forEach(page -> page.setIsCollapsed(true));
    }

}