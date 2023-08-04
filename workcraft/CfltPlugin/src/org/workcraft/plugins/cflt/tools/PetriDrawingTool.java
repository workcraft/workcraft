package org.workcraft.plugins.cflt.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.dom.visual.Positioning;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.utils.EccUtils;
import org.workcraft.plugins.cflt.utils.ExpressionUtils;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.WorkspaceUtils;

public class PetriDrawingTool {

    private final HashMap<String, VisualTransition> transitionMap = new HashMap<>();

    public void drawPetri(Graph inputG, Graph outputG, boolean isSequence, boolean isRoot, Mode mode) {
        VisualPetri petri = WorkspaceUtils.getAs(ExpressionUtils.we, VisualPetri.class);

        ArrayList<ArrayList<String>> edgeCliqueCover = EccUtils.getEcc(isSequence, mode, inputG, outputG);
        HashSet<String> inputVertices = new HashSet<>();

        if (!isSequence) {
            inputVertices.addAll(new ArrayList<>());
        } else {
            inputVertices.addAll(inputG.getVertices());
        }

        // Dealing with isolated vertices
        if (inputG.getIsolatedVertices() != null) {
            for (String vertex : inputG.getIsolatedVertices()) {
                if (!transitionMap.containsKey(vertex) && !isSequence) {
                    VisualPlace place = petri.createPlace(null, null);
                    place.getReferencedComponent().setTokens(1);
                    place.setNamePositioning(Positioning.LEFT);

                    VisualTransition newTransition = petri.createTransition(null, null);
                    transitionMap.put(vertex, newTransition);
                    newTransition.setLabel(ExpressionUtils.labelNameMap.get(vertex));
                    newTransition.setLabelPositioning(Positioning.BOTTOM);
                    newTransition.setNamePositioning(Positioning.LEFT);
                    try {
                        petri.connect(place, newTransition);
                    } catch (InvalidConnectionException e) {
                        e.printStackTrace();
                    }

                } else if (isRoot) {
                    VisualPlace place = petri.createPlace(null, null);
                    VisualTransition transition = transitionMap.get(vertex);
                    transitionMap.put(vertex, transition);
                    place.getReferencedComponent().setTokens(1);

                    try {
                        petri.connect(place, transition);
                    } catch (InvalidConnectionException ignored) {
                    }
                }
            }
        }

        for (ArrayList<String> clique : edgeCliqueCover) {
            // If the clique is not empty
            if ((clique != null) && !clique.isEmpty()) {
                // Get vertices of a single clique from it's edges
                VisualPlace place = petri.createPlace(null, null);
                place.setNamePositioning(Positioning.LEFT);
                boolean connectionsOnlyFromPlaceToTransitions = true;

                for (String v : clique) {
                    boolean isClone = false;
                    String vertex;
                    if (v.contains("$")) {
                        int firstOc = v.indexOf("$");
                        vertex = v.substring(0, firstOc);
                        isClone = true;
                    } else {
                        vertex = v;
                    }

                    VisualTransition newTransition;
                    if (!transitionMap.containsKey(vertex)) {
                        newTransition = petri.createTransition(null, null);
                        transitionMap.put(vertex, newTransition);

                        newTransition.setLabel(ExpressionUtils.labelNameMap.get(vertex));
                        newTransition.setLabelPositioning(Positioning.BOTTOM);
                        newTransition.setNamePositioning(Positioning.LEFT);

                    } else {
                        newTransition = transitionMap.get(vertex);
                    }
                    try {
                        if (inputVertices.contains(vertex) || isClone) {

                            petri.connect(newTransition, place);
                            connectionsOnlyFromPlaceToTransitions = false;
                            if (isRoot) {
                                place.getReferencedComponent().setTokens(1);
                            }
                        } else {
                            petri.connect(place, newTransition);
                        }
                    } catch (InvalidConnectionException ignored) {
                    }
                }
                if (connectionsOnlyFromPlaceToTransitions) {
                    place.getReferencedComponent().setTokens(1);
                }
            }
        }

    }
    public void drawSingleTransition(String name) {
        VisualPetri petri = WorkspaceUtils.getAs(ExpressionUtils.we, VisualPetri.class);

        VisualPlace place = petri.createPlace(null, null);
        place.setNamePositioning(Positioning.LEFT);
        place.getReferencedComponent().setTokens(1);

        VisualTransition newTransition = petri.createTransition(null, null);

        newTransition.setLabel(name);
        newTransition.setLabelPositioning(Positioning.BOTTOM);
        newTransition.setNamePositioning(Positioning.LEFT);

        try {
            petri.connect(place, newTransition);
        } catch (InvalidConnectionException ignored) {
        }
    }

}
