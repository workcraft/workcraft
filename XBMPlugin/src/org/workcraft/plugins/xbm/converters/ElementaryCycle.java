package org.workcraft.plugins.xbm.converters;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.xbm.XbmSignal;

import java.awt.geom.Point2D;

//Takes any signal and converts it into a corresponding elementary cycle in Petri Net form
public class ElementaryCycle {

    public final static String TRANSITION_NAME_RISING = "_PLUS";
    public final static String TRANSITION_NAME_FALLING = "_MINUS";
    public final static String PLACE_NAME_LOW = "_LOW";
    public final static String PLACE_NAME_HIGH = "_HIGH";

    private final VisualPlace low, high;
    private final VisualTransition falling, rising;

    private static double startXDiff = 0;
    private static double startYDiff = 5;
    private static double diff = 5;

    public ElementaryCycle(VisualPetri vPetri, XbmSignal xbmSignal) {
        if (xbmSignal.getType() != XbmSignal.Type.DUMMY) {
            low = generateLowState(vPetri, xbmSignal);
            high = generateHighState(vPetri, xbmSignal);
            falling = generateFallingTransition(vPetri, xbmSignal);
            rising = generateRisingTransition(vPetri, xbmSignal);

            //FIXME Hard-coded position of elementary cycle - will need refactoring later
            Point2D lowPos = low.getPosition();
            lowPos.setLocation(lowPos.getX() + diff + startXDiff, lowPos.getY() + diff + startYDiff);
            low.setPosition(lowPos);

            Point2D highPos = low.getPosition();
            highPos.setLocation(highPos.getX(), highPos.getY() + diff);
            high.setPosition(highPos);

            Point2D fallingPos = low.getPosition();
            fallingPos.setLocation(fallingPos.getX() - diff / 2, fallingPos.getY() + diff / 2);
            falling.setPosition(fallingPos);

            Point2D risingPos = low.getPosition();
            risingPos.setLocation(risingPos.getX() + diff / 2, risingPos.getY() + diff / 2);
            rising.setPosition(risingPos);

            startXDiff += 7.5;

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

    private static final VisualPlace generateLowState(VisualPetri vPetri, XbmSignal xbmSignal) {
        VisualPlace result = vPetri.createPlace(xbmSignal.getName()  + PLACE_NAME_LOW, null);
        result.getReferencedPlace().setTokens(1);
        result.setLabel(xbmSignal.getName() + "=0");
        return result;
    }

    private static final VisualPlace generateHighState(VisualPetri vPetri, XbmSignal xbmSignal) {
        VisualPlace result = vPetri.createPlace(xbmSignal.getName()  + PLACE_NAME_HIGH, null);
        result.getReferencedPlace().setTokens(0);
        result.setLabel(xbmSignal.getName() + "=1");
        return result;
    }

    private static final VisualTransition generateFallingTransition(VisualPetri vPetri, XbmSignal xbmSignal) {
        VisualTransition falling = vPetri.createTransition(xbmSignal.getName() + TRANSITION_NAME_FALLING, null);
        falling.setLabel(xbmSignal.getName() + "-");
        return falling;
    }

    private static final VisualTransition generateRisingTransition(VisualPetri vPetri, XbmSignal xbmSignal) {
        VisualTransition result = vPetri.createTransition(xbmSignal.getName() + TRANSITION_NAME_RISING, null);
        result.setLabel(xbmSignal.getName() + "+");
        return result;
    }
}
