package org.workcraft.plugins.petri.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.traces.Trace;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class PetriUtils {

    private PetriUtils() {
    }

    public static HashMap<Place, Integer> getMarking(PetriModel net) {
        HashMap<Place, Integer> marking = new HashMap<>();
        for (Place place: net.getPlaces()) {
            marking.put(place, place.getTokens());
        }
        return marking;
    }

    public static void setMarking(PetriModel net, HashMap<Place, Integer> marking) {
        for (Place place: net.getPlaces()) {
            Integer count = marking.get(place);
            if (count != null) {
                place.setTokens(count);
            }
        }
    }

    public static boolean fireTrace(PetriModel net, Trace trace) {
        for (String ref: trace) {
            if (ref != null) {
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
        }
        return true;
    }

    public static Set<Transition> getEnabledTransitions(PetriModel net) {
        Set<Transition> result = new HashSet<>();
        for (Transition transition: net.getTransitions()) {
            if (net.isEnabled(transition)) {
                result.add(transition);
            }
        }
        return result;
    }

    public static boolean checkSoundness(PetriModel stg, boolean ask) {
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
            msg += TextUtils.wrapMessageWithItems("\n* Disconnected transition", hangingTransitions);
        }
        if (!unboundedTransitions.isEmpty()) {
            msg += TextUtils.wrapMessageWithItems("\n* Empty preset transition", unboundedTransitions);
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
            msg += TextUtils.wrapMessageWithItems("\n* Disconnected place", hangingPlaces);
        }
        if (!deadPlaces.isEmpty()) {
            msg += TextUtils.wrapMessageWithItems("\n* Dead place", deadPlaces);
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

    public static HashSet<Place> getIsolatedMarkedPlaces(PetriModel model) {
        HashSet<Place> result = new HashSet<>();
        for (Place place: model.getPlaces()) {
            if ((place.getTokens() > 0) && model.getConnections(place).isEmpty()) {
                result.add(place);
            }
        }
        return result;
    }

    public static void removeIsolatedMarkedVisualPlaces(VisualModel visualModel) {
        MathModel model = visualModel.getMathModel();
        for (VisualPlace visualPlace: Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualPlace.class)) {
            Place place = visualPlace.getReferencedComponent();
            if ((place.getTokens() > 0) && model.getConnections(place).isEmpty()) {
                visualModel.remove(visualPlace);
            }
        }
    }

}
