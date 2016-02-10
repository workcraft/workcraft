package org.workcraft.plugins.son.granularity;

import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.util.Interval;

public interface TimeGranularity {

    Integer plusTD(int time, int duration) throws TimeOutOfBoundsException;
    Interval plusTD(Interval time, Interval duration) throws TimeOutOfBoundsException;

    Integer subtractTD(Integer time, Integer duration) throws TimeOutOfBoundsException;
    Interval subtractTD(Interval time, Interval duration) throws TimeOutOfBoundsException;

    Integer subtractTT(Integer start, Integer end) throws TimeOutOfBoundsException;
    Interval subtractTT(Interval start, Interval end) throws TimeOutOfBoundsException;

}
