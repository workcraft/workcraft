package org.workcraft.plugins.xbm.converters;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.plugins.xbm.XbmSignal;

import java.awt.geom.Point2D;

public class StgElementaryCycle {

    public static final String PLACE_NAME_LOW = "_LOW";
    public static final String PLACE_NAME_HIGH = "_HIGH";

    private final VisualStgPlace low;
    private final VisualStgPlace high;
    private final VisualSignalTransition falling;
    private final VisualSignalTransition rising;

    private static double startXDiff = 0;
    private static double startYDiff = 5;
    private static double diff = 5;

    public StgElementaryCycle(VisualStg visualStg, XbmSignal xbmSignal) {
        if (xbmSignal.getType() != XbmSignal.Type.DUMMY) {
            low = generateLowState(visualStg, xbmSignal);
            high = generateHighState(visualStg, xbmSignal);
            falling = generateFallingTransition(visualStg, xbmSignal);
            rising = generateRisingTransition(visualStg, xbmSignal);

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
                visualStg.connect(low, rising);
                visualStg.connect(rising, high);
                visualStg.connect(high, falling);
                visualStg.connect(falling, low);
            } catch (InvalidConnectionException ice) {
                //Remove the places if the check fails
                visualStg.remove(low);
                visualStg.remove(high);
                visualStg.remove(falling);
                visualStg.remove(rising);
            }
        } else {
            low = null;
            high = null;
            falling = null;
            rising = null;
        }
    }

    public VisualStgPlace getLow() {
        return low;
    }

    public VisualStgPlace getHigh() {
        return high;
    }

    public VisualSignalTransition getFalling() {
        return falling;
    }

    public VisualSignalTransition getRising() {
        return rising;
    }

    private static VisualStgPlace generateLowState(VisualStg visualStg, XbmSignal xbmSignal) {
        return createPlace(visualStg, xbmSignal, PLACE_NAME_LOW, 1);
    }

    private static VisualStgPlace generateHighState(VisualStg visualStg, XbmSignal xbmSignal) {
        return createPlace(visualStg, xbmSignal, PLACE_NAME_HIGH, 0);
    }

    private static VisualSignalTransition generateFallingTransition(VisualStg visualStg, XbmSignal xbmSignal) {
        return createTransition(visualStg, xbmSignal, SignalTransition.Direction.MINUS);
    }

    private static VisualSignalTransition generateRisingTransition(VisualStg visualStg, XbmSignal xbmSignal) {
        return createTransition(visualStg, xbmSignal, SignalTransition.Direction.PLUS);
    }

    private static VisualStgPlace createPlace(VisualStg visualStg, XbmSignal xbmSignal, String namePrefix, int tokenCount) {
        final VisualStgPlace result = visualStg.createVisualPlace(xbmSignal.getName() + namePrefix, null);
        final String prefixParse = namePrefix.equals(PLACE_NAME_HIGH) ? "=1" : "=0";
        result.getReferencedComponent().setTokens(tokenCount);
        result.setLabel(xbmSignal.getName() + prefixParse);
        return result;
    }

    private static VisualSignalTransition createTransition(VisualStg visualStg, XbmSignal xbmSignal, SignalTransition.Direction determineDirection) {
        return visualStg.createVisualSignalTransition(xbmSignal.getName(), XbmToStgConversionUtil.getReferredType(xbmSignal.getType()), determineDirection);
    }
}
