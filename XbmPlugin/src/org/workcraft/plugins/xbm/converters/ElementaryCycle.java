package org.workcraft.plugins.xbm.converters;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.xbm.XbmSignal;

import java.awt.geom.Point2D;

public class ElementaryCycle {

    public static final String TRANSITION_NAME_RISING = "_PLUS";
    public static final String TRANSITION_NAME_FALLING = "_MINUS";
    public static final String TRANSITION_NAME_TOGGLE = "_TOGGLE";
    public static final String PLACE_NAME_LOW = "_LOW";
    public static final String PLACE_NAME_HIGH = "_HIGH";

    private final VisualPlace low;
    private final VisualPlace high;
    private final VisualTransition falling;
    private final VisualTransition rising;

    private static double startXDiff = 0;
    private static double startYDiff = 5;
    private static double diff = 5;

    public ElementaryCycle(VisualPetri visualPetri, XbmSignal xbmSignal) {
        low = generateLowState(visualPetri, xbmSignal);
        high = generateHighState(visualPetri, xbmSignal);
        falling = generateFallingTransition(visualPetri, xbmSignal);
        rising = generateRisingTransition(visualPetri, xbmSignal);
        setComponentPositions(visualPetri);
    }

    public ElementaryCycle(VisualStg visualStg, XbmSignal xbmSignal) {
        low = generateLowState(visualStg, xbmSignal);
        high = generateHighState(visualStg, xbmSignal);
        falling = generateFallingTransition(visualStg, xbmSignal);
        rising = generateRisingTransition(visualStg, xbmSignal);
        setComponentPositions(visualStg);
    }

    private void setComponentPositions(VisualModel visualModel) {
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
            //Connect all components to form an elementary cycle
            visualModel.connect(low, rising);
            visualModel.connect(rising, high);
            visualModel.connect(high, falling);
            visualModel.connect(falling, low);
        } catch (InvalidConnectionException ice) {
            //If invalid remove all components
            visualModel.remove(low);
            visualModel.remove(high);
            visualModel.remove(falling);
            visualModel.remove(rising);
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

    private static VisualPlace generateLowState(VisualModel visualModel, XbmSignal xbmSignal) {
        return createPlace(visualModel, xbmSignal, PLACE_NAME_LOW, 1);
    }

    private static VisualPlace generateHighState(VisualModel visualModel, XbmSignal xbmSignal) {
        return createPlace(visualModel, xbmSignal, PLACE_NAME_HIGH, 0);
    }

    private static VisualTransition generateFallingTransition(VisualModel visualModel, XbmSignal xbmSignal) {
        return createTransition(visualModel, xbmSignal, SignalTransition.Direction.MINUS);
    }

    private static VisualTransition generateRisingTransition(VisualModel visualModel, XbmSignal xbmSignal) {
        return createTransition(visualModel, xbmSignal, SignalTransition.Direction.PLUS);
    }

    private static VisualPlace createPlace(VisualModel visualModel, XbmSignal xbmSignal, String namePrefix, int tokenCount) {
        VisualPlace result = null;
        if (visualModel instanceof VisualPetri) {
            final VisualPetri visualPetri = (VisualPetri) visualModel;
            result = visualPetri.createPlace(xbmSignal.getName() + namePrefix, null);
        } else {
            final VisualStg visualStg = (VisualStg) visualModel;
            result = visualStg.createVisualPlace(xbmSignal.getName() + namePrefix, null);
        }
        if (result != null) {
            final String prefixParse = namePrefix.equals(PLACE_NAME_HIGH) ? "=1" : "=0";
            result.getReferencedComponent().setTokens(tokenCount);
            result.setLabel(xbmSignal.getName() + prefixParse);
        }
        return result;
    }

    private static VisualTransition createTransition(VisualModel visualModel, XbmSignal xbmSignal, SignalTransition.Direction direction) {
        VisualTransition result = null;
        if (visualModel instanceof VisualPetri) {
            final VisualPetri visualPetri = (VisualPetri) visualModel;
            result = visualPetri.createTransition(xbmSignal.getName() + convertStgDirectionToPetriNetString(direction), visualPetri.getRoot());
        } else if (visualModel instanceof VisualStg) {
            final VisualStg visualStg = (VisualStg) visualModel;
            result = visualStg.createVisualSignalTransition(xbmSignal.getName(), XbmToStgConversionUtil.getReferredType(xbmSignal.getType()), direction);
        }
        return result;
    }

    private static String convertStgDirectionToPetriNetString(SignalTransition.Direction direction) {
        final String result;
        switch (direction) {
        case PLUS:
            result = TRANSITION_NAME_RISING;
            break;
        case MINUS:
            result = TRANSITION_NAME_FALLING;
            break;
        case TOGGLE:
            result = TRANSITION_NAME_TOGGLE;
            break;
        default:
            result = "";
            break;
        }
        return result;
    }
}
