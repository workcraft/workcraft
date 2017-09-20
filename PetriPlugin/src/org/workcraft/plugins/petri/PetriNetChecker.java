package org.workcraft.plugins.petri;

import java.util.Set;

import org.workcraft.dom.Node;

public class PetriNetChecker {

    public static boolean isPure(PetriNetModel model) {
        for (Place place: model.getPlaces()) {
            for (Node transition: model.getPostset(place)) {
                Set<Node> transitionPostset = model.getPostset(transition);
                if (transitionPostset.contains(place)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isFreeChoice(PetriNetModel model) {
        for (Place place: model.getPlaces()) {
            Set<Node> placePostset = model.getPostset(place);
            if (placePostset.size() > 1) {
                for (Node transition: placePostset) {
                    Set<Node> transitionPreset = model.getPreset(transition);
                    if (transitionPreset.size() > 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isExtendedFreeChoice(PetriNetModel model) {
        for (Place place: model.getPlaces()) {
            Set<Node> placePostset = model.getPostset(place);
            if (placePostset.size() > 1) {
                Set<Node> transitionPreset = null;
                for (Node transition: placePostset) {
                    if (transitionPreset == null) {
                        transitionPreset = model.getPreset(transition);
                    } else {
                        if (!transitionPreset.equals(model.getPreset(transition))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean isMarkedGraph(PetriNetModel model) {
        for (Place place: model.getPlaces()) {
            Set<Node> placePreset = model.getPreset(place);
            Set<Node> placePostset = model.getPostset(place);
            if ((placePreset.size() > 1) || (placePostset.size() > 1)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isStateMachine(PetriNetModel model) {
        for (Transition transition: model.getTransitions()) {
            Set<Node> transitionPreset = model.getPreset(transition);
            Set<Node> transitionPostset = model.getPostset(transition);
            if ((transitionPreset.size() > 1) || (transitionPostset.size() > 1)) {
                return false;
            }
        }
        return true;
    }

}
