package org.workcraft.plugins.petri.utils;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;

import java.util.Set;

public class CheckUtils {

    public static boolean isPure(PetriModel model) {
        for (Place place: model.getPlaces()) {
            for (MathNode transition: model.getPostset(place)) {
                Set<MathNode> transitionPostset = model.getPostset(transition);
                if (transitionPostset.contains(place)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isFreeChoice(PetriModel model) {
        for (Place place: model.getPlaces()) {
            Set<MathNode> placePostset = model.getPostset(place);
            if (placePostset.size() > 1) {
                for (MathNode transition: placePostset) {
                    Set<MathNode> transitionPreset = model.getPreset(transition);
                    if (transitionPreset.size() > 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isExtendedFreeChoice(PetriModel model) {
        for (Place place: model.getPlaces()) {
            Set<MathNode> placePostset = model.getPostset(place);
            if (placePostset.size() > 1) {
                Set<MathNode> transitionPreset = null;
                for (MathNode transition: placePostset) {
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

    public static boolean isMarkedGraph(PetriModel model) {
        for (Place place: model.getPlaces()) {
            Set<MathNode> placePreset = model.getPreset(place);
            Set<MathNode> placePostset = model.getPostset(place);
            if ((placePreset.size() > 1) || (placePostset.size() > 1)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isStateMachine(PetriModel model) {
        for (Transition transition: model.getTransitions()) {
            Set<MathNode> transitionPreset = model.getPreset(transition);
            Set<MathNode> transitionPostset = model.getPostset(transition);
            if ((transitionPreset.size() > 1) || (transitionPostset.size() > 1)) {
                return false;
            }
        }
        return true;
    }

}
