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

    HashMap<String, VisualTransition> vTransitionMap = new HashMap<>();

    public void drawPetri(Graph inputG, Graph outputG, boolean isSequence, boolean isRoot, Mode mode) {
        String eccOption = mode.toString();
        VisualPetri vPetri = WorkspaceUtils.getAs(ExpressionUtils.we, VisualPetri.class);

        ArrayList<ArrayList<String>> edgeCliqueCover = EccUtils.getEcc(isSequence, eccOption, inputG, outputG);
        HashSet<String> inputVertices = new HashSet<>();

        if (!isSequence) {
            inputVertices.addAll(new ArrayList<>());
        } else {
            inputVertices.addAll(inputG.getVertices());
        }

        // Dealing with isolated vertices
        if (inputG.getIsolatedVertices() != null) {
            for (String vertex : inputG.getIsolatedVertices()) {
                if (!vTransitionMap.containsKey(vertex) && !isSequence) {
                    VisualPlace vPlace = vPetri.createPlace(null, null);
                    vPlace.getReferencedComponent().setTokens(1);
                    vPlace.setNamePositioning(Positioning.LEFT);

                    VisualTransition newVtransition = vPetri.createTransition(null, null);
                    vTransitionMap.put(vertex, newVtransition);
                    newVtransition.setLabel(ExpressionUtils.labelNameMap.get(vertex));
                    newVtransition.setLabelPositioning(Positioning.BOTTOM);
                    newVtransition.setNamePositioning(Positioning.LEFT);
                    try {
                        vPetri.connect(vPlace, newVtransition);
                    } catch (InvalidConnectionException e) {
                        e.printStackTrace();
                    }

                } else if (isRoot) {
                    VisualPlace vPlace = vPetri.createPlace(null, null);
                    VisualTransition vTransition = vTransitionMap.get(vertex);
                    vTransitionMap.put(vertex, vTransition);
                    vPlace.getReferencedComponent().setTokens(1);

                    try {
                        vPetri.connect(vPlace, vTransition);
                    } catch (InvalidConnectionException ignored) {
                    }
                }
            }
        }

        for (ArrayList<String> clique : edgeCliqueCover) {
            // If the clique is not empty
            if ((clique != null) && !clique.isEmpty()) {
                // Get vertices of a single clique from it's edges
                VisualPlace vPlace = vPetri.createPlace(null, null);
                vPlace.setNamePositioning(Positioning.LEFT);
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

                    VisualTransition newVtransition;
                    if (!vTransitionMap.containsKey(vertex)) {
                        newVtransition = vPetri.createTransition(null, null);
                        vTransitionMap.put(vertex, newVtransition);

                        newVtransition.setLabel(ExpressionUtils.labelNameMap.get(vertex));
                        newVtransition.setLabelPositioning(Positioning.BOTTOM);
                        newVtransition.setNamePositioning(Positioning.LEFT);

                    } else {
                        newVtransition = vTransitionMap.get(vertex);
                    }
                    try {
                        if (inputVertices.contains(vertex) || isClone) {

                            vPetri.connect(newVtransition, vPlace);
                            connectionsOnlyFromPlaceToTransitions = false;
                            if (isRoot) {
                                vPlace.getReferencedComponent().setTokens(1);
                            }
                        } else {
                            vPetri.connect(vPlace, newVtransition);
                        }
                    } catch (InvalidConnectionException ignored) {
                    }
                }
                if (connectionsOnlyFromPlaceToTransitions) {
                    vPlace.getReferencedComponent().setTokens(1);
                }
            }
        }

    }
    public void drawSingleTransition(String name) {
        VisualPetri vPetri = WorkspaceUtils.getAs(ExpressionUtils.we, VisualPetri.class);

        VisualPlace vPlace = vPetri.createPlace(null, null);
        vPlace.setNamePositioning(Positioning.LEFT);
        vPlace.getReferencedComponent().setTokens(1);

        VisualTransition newVtransition = vPetri.createTransition(null, null);

        newVtransition.setLabel(name);
        newVtransition.setLabelPositioning(Positioning.BOTTOM);
        newVtransition.setNamePositioning(Positioning.LEFT);

        try {
            vPetri.connect(vPlace, newVtransition);
        } catch (InvalidConnectionException ignored) {
        }
    }

}
