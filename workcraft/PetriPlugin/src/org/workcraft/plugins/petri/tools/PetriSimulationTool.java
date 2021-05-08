package org.workcraft.plugins.petri.tools;

import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.dialogs.ExceptionDialog;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SimulationTool;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.converters.PetriToPetriConverter;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.shared.ColorGenerator;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.util.*;

public class PetriSimulationTool extends SimulationTool {

    private final Set<Place> badCapacityPlaces = new HashSet<>();
    private PetriToPetriConverter converter;

    public PetriSimulationTool() {
        this(false);
    }

    public PetriSimulationTool(boolean enableTraceGraph) {
        super(enableTraceGraph);
    }

    @Override
    public void generateUnderlyingModel(WorkspaceEntry we) {
        converter = new PetriToPetriConverter(WorkspaceUtils.getAs(we, VisualPetri.class));
    }

    @Override
    public PetriModel getUnderlyingModel() {
        return converter.getDstModel().getMathModel();
    }

    public VisualModel getUnderlyingVisualModel() {
        return converter.getDstModel();
    }

    @Override
    public void activated(GraphEditor editor) {
        super.activated(editor);
        badCapacityPlaces.clear();
    }

    @Override
    public boolean isConnectionExcited(VisualModel model, VisualConnection connection) {
        VisualNode first = connection.getFirst();
        String ref = null;
        if (first instanceof VisualPlace) {
            VisualPlace place = (VisualPlace) first;
            ref = model.getMathReference(place.getReferencedComponent());
        } else if (first instanceof VisualReplicaPlace) {
            VisualReplicaPlace replicaPlace = (VisualReplicaPlace) first;
            ref = model.getMathReference(replicaPlace.getReferencedPlace());
        }
        MathNode underlyingNode = getUnderlyingNode(ref);
        return (underlyingNode instanceof Place) && (((Place) underlyingNode).getTokens() > 0);
    }

    @Override
    public HashMap<? extends MathNode, Integer> readUnderlyingModelState() {
        return PetriUtils.getMarking(getUnderlyingModel());
    }

    @Override
    public void writeUnderlyingModelState(Map<? extends MathNode, Integer> state) {
        HashSet<Place> places = new HashSet<>(getUnderlyingModel().getPlaces());
        for (MathNode node : state.keySet()) {
            if (node instanceof Place) {
                Place place = (Place) node;
                if (places.contains(place)) {
                    place.setTokens(state.get(place));
                } else {
                    ExceptionDialog.show(new RuntimeException("Place " + place + " is not in the model"));
                }
            }
        }
    }

    @Override
    public void applySavedState(GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof PetriModel) {
            PetriModel petri = (PetriModel) model;
            editor.getWorkspaceEntry().saveMemento();
            for (Place place : petri.getPlaces()) {
                String ref = petri.getNodeReference(place);
                MathNode underlyingNode = getUnderlyingNode(ref);
                if ((underlyingNode instanceof Place) && savedState.containsKey(underlyingNode)) {
                    Integer tokens = savedState.get(underlyingNode);
                    place.setTokens(tokens);
                }
            }
        }
    }

    @Override
    public boolean isEnabledUnderlyingNode(MathNode node) {
        boolean result = false;
        PetriModel petri = getUnderlyingModel();
        if ((petri != null) && (node instanceof Transition)) {
            Transition transition = (Transition) node;
            result = petri.isEnabled(transition);
        }
        return result;
    }

    @Override
    public ArrayList<? extends MathNode> getEnabledUnderlyingNodes() {
        ArrayList<MathNode> result = new ArrayList<>();
        for (Transition transition : getUnderlyingModel().getTransitions()) {
            if (isEnabledUnderlyingNode(transition)) {
                result.add(transition);
            }
        }
        return result;
    }

    @Override
    public boolean fire(String ref) {
        boolean result = false;
        Transition transition = null;
        PetriModel petri = getUnderlyingModel();
        if (ref != null) {
            MathNode node = getUnderlyingNode(ref);
            if (node instanceof Transition) {
                transition = (Transition) node;
            }
        }
        if (isEnabledUnderlyingNode(transition)) {
            HashMap<Place, Integer> capacity = new HashMap<>();
            for (MathNode node : petri.getPostset(transition)) {
                if (node instanceof Place) {
                    Place place = (Place) node;
                    capacity.put(place, place.getCapacity());
                }
            }
            petri.fire(transition);
            coloriseTokens(transition);
            Set<String> placeRefs = new HashSet<>();
            for (MathNode node : petri.getPostset(transition)) {
                if (node instanceof Place) {
                    Place place = (Place) node;
                    if ((place.getCapacity() > capacity.get(place)) && !badCapacityPlaces.contains(place)) {
                        badCapacityPlaces.add(place);
                        placeRefs.add(petri.getNodeReference(place));
                    }
                }
            }
            if (!placeRefs.isEmpty()) {
                DialogUtils.showWarning(TextUtils.wrapMessageWithItems("Promised capacity is violated for place", placeRefs));
            }
            result = true;
        }
        return result;
    }

    @Override
    public boolean unfire(String ref) {
        boolean result = false;
        Transition transition = null;
        PetriModel petri = getUnderlyingModel();
        if (ref != null) {
            MathNode node = getUnderlyingNode(ref);
            if (node instanceof Transition) {
                transition = (Transition) node;
            }
        }
        if (transition != null) {
            if (petri.isUnfireEnabled(transition)) {
                petri.unFire(transition);
                result = true;
            }
        }
        return result;
    }

    @Override
    public String getHintText(GraphEditor editor) {
        return "Click on a highlighted transition to fire it.";
    }

    public void coloriseTokens(Transition transition) {
        VisualModel model = getUnderlyingVisualModel();
        VisualPetri petri = (model instanceof VisualPetri) ? (VisualPetri) model : null;
        if (petri == null) {
            return;
        }

        VisualTransition vt = petri.getVisualTransition(transition);
        if (vt == null) {
            return;
        }

        Color tokenColor = Color.BLACK;
        ColorGenerator tokenColorGenerator = vt.getTokenColorGenerator();
        if (tokenColorGenerator != null) {
            // generate token colour
            tokenColor = tokenColorGenerator.updateColor();
        } else {
            // combine preset token colours
            for (VisualConnection vc : petri.getConnections(vt)) {
                if (vc.isTokenColorPropagator() && (vc.getFirst() instanceof VisualPlace) && (vc.getSecond() == vt)) {
                    VisualPlace vp = (VisualPlace) vc.getFirst();
                    tokenColor = ColorUtils.colorise(tokenColor, vp.getTokenColor());
                }
            }
        }
        // propagate the colour to postset tokens
        for (VisualConnection vc : petri.getConnections(vt)) {
            if (vc.isTokenColorPropagator() && (vc.getFirst() == vt) && (vc.getSecond() instanceof VisualPlace)) {
                VisualPlace vp = (VisualPlace) vc.getSecond();
                vp.setTokenColor(tokenColor);
            }
        }
    }

    @Override
    public Decoration getComponentDecoration(VisualModel model, VisualComponent component) {
        VisualPlace underlyingVisualPlace = getUnderlyingVisualPlace(model, component);
        if (underlyingVisualPlace == null) {
            return super.getComponentDecoration(model, component);
        }
        return new PlaceDecoration() {
            @Override
            public Color getColorisation() {
                return null;
            }

            @Override
            public Color getBackground() {
                return null;
            }

            @Override
            public int getTokens() {
                return underlyingVisualPlace.getReferencedComponent().getTokens();
            }

            @Override
            public Color getTokenColor() {
                return underlyingVisualPlace.getTokenColor();
            }
        };
    }

    private VisualPlace getUnderlyingVisualPlace(VisualModel model, VisualComponent component) {
        VisualModel underlyingVisualModel = getUnderlyingVisualModel();
        if ((component instanceof VisualPlace) && (underlyingVisualModel != null)) {
            String ref = model.getMathReference(component);
            return underlyingVisualModel.getVisualComponentByMathReference(ref, VisualPlace.class);
        }
        return null;
    }

}
