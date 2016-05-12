package org.workcraft.plugins.son.granularity;

import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.util.Interval;

public abstract class AbstractTimeGranularity implements TimeGranularity {

    public abstract Integer plusTD(int time, int duration) throws TimeOutOfBoundsException;

    @Override
    public Interval plusTD(Interval time, Interval duration) throws TimeOutOfBoundsException {
        Interval result = new Interval();
        int minT = time.getMin();
        int maxT = time.getMax();
        int minD = duration.getMin();
        int maxD = duration.getMax();

        result.setMin(plusTD(minT, minD));
        result.setMax(plusTD(maxT, maxD));

        return result;
    }

    public abstract Integer subtractTD(Integer time, Integer duration) throws TimeOutOfBoundsException;

    @Override
    public Interval subtractTD(Interval time, Interval duration) throws TimeOutOfBoundsException {
        Interval result = new Interval();
        int minT = time.getMin();
        int maxT = time.getMax();
        int minD = duration.getMin();
        int maxD = duration.getMax();

        result.setMin(subtractTD(minT, maxD));
        result.setMax(subtractTD(maxT, minD));

        return result;
    }

    public abstract Integer subtractTT(Integer start, Integer end) throws TimeOutOfBoundsException;

    @Override
    public Interval subtractTT(Interval start, Interval end) throws TimeOutOfBoundsException {
        Interval result = new Interval();
        int minS = start.getMin();
        int maxS = start.getMax();
        int minF = end.getMin();
        int maxF = end.getMax();

        result.setMin(subtractTT(maxS, minF));
        result.setMax(subtractTT(minS, maxF));

        return result;
    }
}
