package org.workcraft.plugins.petri.tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SimulationTool;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.LogUtils;

public class PetriSimulationTool extends SimulationTool {

    public PetriSimulationTool() {
        this(false);
    }

    public PetriSimulationTool(boolean enableTraceGraph) {
        super(enableTraceGraph);
    }

    public PetriNetModel getUnderlyingPetri() {
        return (PetriNetModel) getUnderlyingModel().getMathModel();
    }

    @Override
    public boolean isConnectionExcited(VisualConnection connection) {
        VisualNode first = connection.getFirst();
        Place place = null;
        if (first instanceof VisualPlace) {
            place = ((VisualPlace) first).getReferencedPlace();
        } else if (first instanceof VisualReplicaPlace) {
            place = ((VisualReplicaPlace) first).getReferencedPlace();
        }
        return (place != null) && (place.getTokens() > 0);
    }

    @Override
    public HashMap<? extends Node, Integer> readModelState() {
        return PetriUtils.getMarking(getUnderlyingPetri());
    }

    @Override
    public void writeModelState(Map<? extends Node, Integer> state) {
        HashSet<Place> places = new HashSet<>(getUnderlyingPetri().getPlaces());
        for (Node node: state.keySet()) {
            if (node instanceof Place) {
                Place place = (Place) node;
                if (places.contains(place)) {
                    place.setTokens(state.get(place));
                } else {
                    ExceptionDialog.show(new RuntimeException("Place " + place.toString() + " is not in the model"));
                }
            }
        }
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof PetriNetModel) {
            PetriNetModel petri = (PetriNetModel) model;
            editor.getWorkspaceEntry().saveMemento();
            for (Place place: petri.getPlaces()) {
                String ref = petri.getNodeReference(place);
                Node underlyingNode = getUnderlyingPetri().getNodeByReference(ref);
                if ((underlyingNode instanceof Place) && savedState.containsKey(underlyingNode)) {
                    Integer tokens = savedState.get(underlyingNode);
                    place.setTokens(tokens);
                }
            }
        }
    }

    @Override
    public boolean isEnabledNode(Node node) {
        boolean result = false;
        PetriNetModel petri = getUnderlyingPetri();
        if ((petri != null) && (node instanceof Transition)) {
            Transition transition = (Transition) node;
            result = petri.isEnabled(transition);
        }
        return result;
    }

    @Override
    public ArrayList<Node> getEnabledNodes() {
        ArrayList<Node> result = new ArrayList<>();
        for (Transition transition: getUnderlyingPetri().getTransitions()) {
            if (isEnabledNode(transition)) {
                result.add(transition);
            }
        }
        return result;
    }

    @Override
    public boolean fire(String ref) {
        boolean result = false;
        Transition transition = null;
        PetriNetModel petri = getUnderlyingPetri();
        if (ref != null) {
            final Node node = petri.getNodeByReference(ref);
            if (node instanceof Transition) {
                transition = (Transition) node;
            }
        }
        if (isEnabledNode(transition)) {
            HashMap<Place, Integer> capacity = new HashMap<>();
            for (Node node: petri.getPostset(transition)) {
                if (node instanceof Place) {
                    Place place = (Place) node;
                    capacity.put(place, place.getCapacity());
                }
            }
            petri.fire(transition);
            coloriseTokens(transition);
            for (Node node: petri.getPostset(transition)) {
                if (node instanceof Place) {
                    Place place = (Place) node;
                    if (place.getCapacity() > capacity.get(place)) {
                        String placeRef = petri.getNodeReference(place);
                        LogUtils.logWarning("Capacity of place '" + placeRef + "' is incresed to " + place.getCapacity() + ".");
                    }
                }
            }
            result = true;
        }
        return result;
    }

    @Override
    public boolean unfire(String ref) {
        boolean result = false;
        Transition transition = null;
        if (ref != null) {
            final Node node = getUnderlyingPetri().getNodeByReference(ref);
            if (node instanceof Transition) {
                transition = (Transition) node;
            }
        }
        if (transition != null) {
            if (getUnderlyingPetri().isUnfireEnabled(transition)) {
                getUnderlyingPetri().unFire(transition);
                result = true;
            }
        }
        return result;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted transition to fire it.";
    }

    protected void coloriseTokens(Transition transition) {
        VisualPetriNet model = (VisualPetriNet) getUnderlyingModel();
        VisualTransition vt = model.getVisualTransition(transition);
        if (vt == null) return;
        Color tokenColor = Color.black;
        ColorGenerator tokenColorGenerator = vt.getTokenColorGenerator();
        if (tokenColorGenerator != null) {
            // generate token colour
            tokenColor = tokenColorGenerator.updateColor();
        } else {
            // combine preset token colours
            for (Connection c: model.getConnections(vt)) {
                if ((c.getSecond() == vt) && (c instanceof VisualConnection)) {
                    VisualConnection vc = (VisualConnection) c;
                    if (vc.isTokenColorPropagator() && (vc.getFirst() instanceof VisualPlace)) {
                        VisualPlace vp = (VisualPlace) vc.getFirst();
                        tokenColor = Coloriser.colorise(tokenColor, vp.getTokenColor());
                    }
                }
            }
        }
        // propagate the colour to postset tokens
        for (Connection c: model.getConnections(vt)) {
            if ((c.getFirst() == vt) && (c instanceof VisualConnection)) {
                VisualConnection vc = (VisualConnection) c;
                if (vc.isTokenColorPropagator() && (vc.getSecond() instanceof VisualPlace)) {
                    VisualPlace vp = (VisualPlace) vc.getFirst();
                    vp.setTokenColor(tokenColor);
                }
            }
        }
    }

}
