package org.workcraft.plugins.xbm.converters;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.xbm.Signal;

//Takes any signal and converts it into a corresponding elementary cycle in Petri Net form
public class ElementaryCycle {

    public final static String TRANSITION_NAME_RISING = "_PLUS";
    public final static String TRANSITION_NAME_FALLING = "_MINUS";
    public final static String PLACE_NAME_LOW = "_LOW";
    public final static String PLACE_NAME_HIGH = "_HIGH";

    private final VisualPlace low, high;
    private final VisualTransition falling, rising;

    public ElementaryCycle(VisualPetri vPetri, Signal signal) {
        if (signal.getType() != Signal.Type.DUMMY) {
            low = generateLowState(vPetri, signal);
            high = generateHighState(vPetri, signal);
            falling = generateFallingTransition(vPetri, signal);
            rising = generateRisingTransition(vPetri, signal);

            try {
                //Elementary cycle
                vPetri.connect(low, rising);
                vPetri.connect(rising, high);
                vPetri.connect(high, falling);
                vPetri.connect(falling, low);
            }
            catch (InvalidConnectionException ice) {
                //Remove the places if the check fails
                vPetri.remove(low);
                vPetri.remove(high);
                vPetri.remove(falling);
                vPetri.remove(rising);
            }
        }
        else {
            low = null;
            high = null;
            falling = null;
            rising = null;
        }
    }

    public VisualPlace getLow() {
        return low;
    }

    public VisualPlace getHigh() {
        return high;
    }

    public VisualTransition getFalling() {
        return falling;
    }

    public VisualTransition getRising() {
        return rising;
    }

    private final static VisualPlace generateLowState(VisualPetri vPetri, Signal signal) {
        VisualPlace result = vPetri.createPlace(signal.getName()  + PLACE_NAME_LOW, null);
        result.getReferencedPlace().setTokens(1);
        result.setLabel(signal.getName() + "=0");
        return result;
    }

    private final static VisualPlace generateHighState(VisualPetri vPetri, Signal signal) {
        VisualPlace result = vPetri.createPlace(signal.getName()  + PLACE_NAME_HIGH, null);
        result.getReferencedPlace().setTokens(0);
        result.setLabel(signal.getName() + "=1");
        return result;
    }

    private final static VisualTransition generateFallingTransition(VisualPetri vPetri, Signal signal) {
        VisualTransition falling = vPetri.createTransition(signal.getName() + TRANSITION_NAME_FALLING, null);
        falling.setLabel(signal.getName() + "-");
        return falling;
    }

    private final static VisualTransition generateRisingTransition(VisualPetri vPetri, Signal signal) {
        VisualTransition result = vPetri.createTransition(signal.getName() + TRANSITION_NAME_RISING, null);
        result.setLabel(signal.getName() + "+");
        return result;
    }
}
