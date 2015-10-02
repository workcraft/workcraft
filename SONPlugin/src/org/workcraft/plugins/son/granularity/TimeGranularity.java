package org.workcraft.plugins.son.granularity;

import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.util.Interval;

public interface TimeGranularity {

	public Integer plusTD(int time, int duration) throws TimeOutOfBoundsException;
	public Interval plusTD(Interval time, Interval duration) throws TimeOutOfBoundsException;

	public Integer subtractTD(Integer time, Integer duration) throws TimeOutOfBoundsException;
	public Interval subtractTD(Interval time, Interval duration) throws TimeOutOfBoundsException;

	public Integer subtractTT(Integer start, Integer end) throws TimeOutOfBoundsException;
	public Interval subtractTT(Interval start, Interval end) throws TimeOutOfBoundsException;

}
