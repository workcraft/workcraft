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
import org.workcraft.plugins.stg.Signal.Type;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.utils.WorkspaceUtils;

public class StgDrawingTool {

    HashMap<String, VisualSignalTransition> vTransitionMap = new HashMap<>();

    public void drawStg(Graph inputG, Graph outputG, boolean isSequence, boolean isRoot, Mode mode) {
        String eccOption = mode.toString();
        VisualStg vStg = WorkspaceUtils.getAs(ExpressionUtils.we, VisualStg.class);

        ArrayList<ArrayList<String>> edgeCliqueCover = EccUtils.getEcc(isSequence, eccOption, inputG, outputG);
        HashSet<String> inputVertices = new HashSet<>();

        if (!isSequence) {
            inputVertices.addAll(new ArrayList<String>());
        } else {
            inputVertices.addAll(inputG.getVertices());
        }

        // Dealing with isolated vertices
        if (inputG.getIsolatedVertices() != null) {
            for (String vertex : inputG.getIsolatedVertices()) {
                if (!vTransitionMap.containsKey(vertex) && !isSequence) {
                    VisualStgPlace vPlace = vStg.createVisualPlace(null);
                    vPlace.getReferencedComponent().setTokens(1);
                    vPlace.setNamePositioning(Positioning.LEFT);

                    VisualSignalTransition newVtransition = vStg.createVisualSignalTransition(
                            ExpressionUtils.labelNameMap.get(vertex), Type.INTERNAL, getDirection(vertex));

                    vTransitionMap.put(vertex, newVtransition);

                    newVtransition.setLabelPositioning(Positioning.BOTTOM);
                    newVtransition.setNamePositioning(Positioning.LEFT);
                    try {
                        vStg.connect(vPlace, newVtransition);
                    } catch (InvalidConnectionException e) {
                        e.printStackTrace();
                    }
                } else if (isRoot) {
                    VisualStgPlace vPlace = vStg.createVisualPlace(null);
                    VisualSignalTransition vTransition = vTransitionMap.get(vertex);
                    vTransitionMap.put(vertex, vTransition);
                    vPlace.getReferencedComponent().setTokens(1);

                    try {
                        vStg.connect(vPlace, vTransition);
                    } catch (InvalidConnectionException ignored) {
                    }
                }
            }
        }
        for (ArrayList<String> clique : edgeCliqueCover) {
            // If the clique is not empty
            if (!clique.isEmpty()) {
                // Get vertices of a single clique from it's edges
                VisualStgPlace vPlace = vStg.createVisualPlace(null);
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
                    VisualSignalTransition newVtransition;
                    if (!vTransitionMap.containsKey(vertex)) {
                        newVtransition = vStg.createVisualSignalTransition(ExpressionUtils.labelNameMap.get(vertex),
                                Type.INTERNAL, getDirection(vertex));

                        vTransitionMap.put(vertex, newVtransition);
                        newVtransition.setLabelPositioning(Positioning.BOTTOM);
                        newVtransition.setNamePositioning(Positioning.LEFT);

                    } else {
                        newVtransition = vTransitionMap.get(vertex);
                    }

                    try {
                        if (inputVertices.contains(vertex) || isClone) {
                            vStg.connect(newVtransition, vPlace);
                            connectionsOnlyFromPlaceToTransitions = false;
                            if (isRoot) {
                                vPlace.getReferencedComponent().setTokens(1);
                            }
                        } else {
                            vStg.connect(vPlace, newVtransition);
                        }
                    } catch (InvalidConnectionException ignored) {

                    }
                }
                if (connectionsOnlyFromPlaceToTransitions) {
                    vPlace.getReferencedComponent().setTokens(1);
                }
            }
        }
        makePlacesImplicit(vStg);
    }

    private void makePlacesImplicit(VisualStg vStg) {
        for (VisualStgPlace vPlace : vStg.getVisualPlaces()) {
            vStg.makeImplicitIfPossible(vPlace, true);
        }
    }

    public void drawSingleTransition(String name) {
        VisualStg vStg = WorkspaceUtils.getAs(ExpressionUtils.we, VisualStg.class);

        VisualStgPlace vPlace = vStg.createVisualPlace(null);
        vPlace.setNamePositioning(Positioning.LEFT);
        vPlace.getReferencedComponent().setTokens(1);

        if (name.charAt(name.length() - 1) == '+') {
            name = name.substring(0, name.length() - 1);
            ExpressionUtils.nameDirectionMap.put(name, ExpressionUtils.PLUS_DIR);
        } else if (name.charAt(name.length() - 1) == '-') {
            name = name.substring(0, name.length() - 1);
            ExpressionUtils.nameDirectionMap.put(name, ExpressionUtils.MINUS_DIR);
        } else {
            ExpressionUtils.nameDirectionMap.put(name, ExpressionUtils.TOGGLE_DIR);
        }
        VisualSignalTransition newVtransition =
                vStg.createVisualSignalTransition(name, Type.INTERNAL, getDirection(name));
        try {
            vStg.connect(vPlace, newVtransition);
        } catch (InvalidConnectionException ignored) {

        }
    }

    private Direction getDirection(String name) {
        char dir = ExpressionUtils.nameDirectionMap.get(name);
        if (dir == '-') {
            return Direction.MINUS;
        } else if (dir == '+') {
            return Direction.PLUS;
        } else {
            return Direction.TOGGLE;
        }
    }

}
