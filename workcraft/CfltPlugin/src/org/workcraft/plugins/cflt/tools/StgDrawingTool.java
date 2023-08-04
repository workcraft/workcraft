package org.workcraft.plugins.cflt.tools;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class StgDrawingTool {

    private final HashMap<String, VisualSignalTransition> transitionMap = new HashMap<>();

    public void drawStg(Graph inputG, Graph outputG, boolean isSequence, boolean isRoot, Mode mode) {
        VisualStg stg = WorkspaceUtils.getAs(ExpressionUtils.we, VisualStg.class);

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
                    VisualStgPlace place = stg.createVisualPlace(null);
                    place.getReferencedComponent().setTokens(1);
                    place.setNamePositioning(Positioning.LEFT);

                    VisualSignalTransition newTransition = stg.createVisualSignalTransition(
                            ExpressionUtils.labelNameMap.get(vertex), Type.INTERNAL, getDirection(vertex));

                    transitionMap.put(vertex, newTransition);

                    newTransition.setLabelPositioning(Positioning.BOTTOM);
                    newTransition.setNamePositioning(Positioning.LEFT);
                    try {
                        stg.connect(place, newTransition);
                    } catch (InvalidConnectionException e) {
                        e.printStackTrace();
                    }
                } else if (isRoot) {
                    VisualStgPlace place = stg.createVisualPlace(null);
                    VisualSignalTransition transition = transitionMap.get(vertex);
                    transitionMap.put(vertex, transition);
                    place.getReferencedComponent().setTokens(1);

                    try {
                        stg.connect(place, transition);
                    } catch (InvalidConnectionException ignored) {
                    }
                }
            }
        }
        for (ArrayList<String> clique : edgeCliqueCover) {
            // If the clique is not empty
            if (!clique.isEmpty()) {
                // Get vertices of a single clique from it's edges
                VisualStgPlace place = stg.createVisualPlace(null);
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
                    VisualSignalTransition newTransition;
                    if (!transitionMap.containsKey(vertex)) {
                        newTransition = stg.createVisualSignalTransition(ExpressionUtils.labelNameMap.get(vertex),
                                Type.INTERNAL, getDirection(vertex));

                        transitionMap.put(vertex, newTransition);
                        newTransition.setLabelPositioning(Positioning.BOTTOM);
                        newTransition.setNamePositioning(Positioning.LEFT);

                    } else {
                        newTransition = transitionMap.get(vertex);
                    }

                    try {
                        if (inputVertices.contains(vertex) || isClone) {
                            stg.connect(newTransition, place);
                            connectionsOnlyFromPlaceToTransitions = false;
                            if (isRoot) {
                                place.getReferencedComponent().setTokens(1);
                            }
                        } else {
                            stg.connect(place, newTransition);
                        }
                    } catch (InvalidConnectionException ignored) {

                    }
                }
                if (connectionsOnlyFromPlaceToTransitions) {
                    place.getReferencedComponent().setTokens(1);
                }
            }
        }
        makePlacesImplicit(stg);
    }

    private void makePlacesImplicit(VisualStg stg) {
        for (VisualStgPlace place : stg.getVisualPlaces()) {
            stg.makeImplicitIfPossible(place, true);
        }
    }

    public void drawSingleTransition(String name) {
        VisualStg stg = WorkspaceUtils.getAs(ExpressionUtils.we, VisualStg.class);

        VisualStgPlace place = stg.createVisualPlace(null);
        place.setNamePositioning(Positioning.LEFT);
        place.getReferencedComponent().setTokens(1);

        if (name.charAt(name.length() - 1) == '+') {
            name = name.substring(0, name.length() - 1);
            ExpressionUtils.nameDirectionMap.put(name, ExpressionUtils.PLUS_DIR);
        } else if (name.charAt(name.length() - 1) == '-') {
            name = name.substring(0, name.length() - 1);
            ExpressionUtils.nameDirectionMap.put(name, ExpressionUtils.MINUS_DIR);
        } else {
            ExpressionUtils.nameDirectionMap.put(name, ExpressionUtils.TOGGLE_DIR);
        }
        VisualSignalTransition newTransition =
                stg.createVisualSignalTransition(name, Type.INTERNAL, getDirection(name));
        try {
            stg.connect(place, newTransition);
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
