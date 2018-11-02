package org.workcraft.plugins.petri.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;

public class PetriUtils {

    public static HashMap<Place, Integer> getMarking(PetriNetModel net) {
        HashMap<Place, Integer> marking = new HashMap<>();
        for (Place place: net.getPlaces()) {
            marking.put(place, place.getTokens());
        }
        return marking;
    }

    public static void setMarking(PetriNetModel net, HashMap<Place, Integer> marking) {
        for (Place place: net.getPlaces()) {
            Integer count = marking.get(place);
            if (count != null) {
                place.setTokens(count);
            }
        }
    }

    public static boolean fireTrace(PetriNetModel net, Trace trace) {
        for (String ref: trace) {
            Node node = net.getNodeByReference(ref);
            if (node instanceof Transition) {
                Transition transition = (Transition) node;
                if (net.isEnabled(transition)) {
                    net.fire(transition);
                } else {
                    LogUtils.logError("Trace transition '" + ref + "' is not enabled.");
                    return false;
                }
            } else {
                LogUtils.logError("Trace transition '" + ref + "' cannot be found.");
                return false;
            }
        }
        return true;
    }

    public static HashSet<Transition> getEnabledTransitions(PetriNetModel net) {
        HashSet<Transition> result = new HashSet<>();
        for (Transition transition: net.getTransitions()) {
            if (net.isEnabled(transition)) {
                result.add(transition);
            }
        }
        return result;
    }

    public static boolean checkSoundness(PetriNetModel stg, boolean ask) {
        String msg = "";
        Set<String> hangingTransitions = new HashSet<>();
        Set<String> unboundedTransitions = new HashSet<>();
        for (Transition transition : stg.getTransitions()) {
            if (stg.getPreset(transition).isEmpty()) {
                String ref = stg.getNodeReference(transition);
                if (stg.getPostset(transition).isEmpty()) {
                    hangingTransitions.add(ref);
                } else {
                    unboundedTransitions.add(ref);
                }
            }
        }
        if (!hangingTransitions.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Disconnected transition", hangingTransitions);
        }
        if (!unboundedTransitions.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Empty preset transition", unboundedTransitions);
        }

        Set<String> hangingPlaces = new HashSet<>();
        Set<String> deadPlaces = new HashSet<>();
        for (Place place : stg.getPlaces()) {
            if (stg.getPreset(place).isEmpty()) {
                String ref = stg.getNodeReference(place);
                if (stg.getPostset(place).isEmpty()) {
                    hangingPlaces.add(ref);
                } else if (place.getTokens() == 0) {
                    deadPlaces.add(ref);
                }
            }
        }
        if (!hangingPlaces.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Disconnected place", hangingPlaces);
        }
        if (!deadPlaces.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Dead place", deadPlaces);
        }

        if (!msg.isEmpty()) {
            msg = "The model has the following issues:" + msg;
            if (ask) {
                msg += "\n\n Proceed anyway?";
                return DialogUtils.showConfirmWarning(msg, "Model validation", false);
            } else {
                DialogUtils.showWarning(msg);
            }
        }
        return true;
    }

}
