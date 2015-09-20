package org.workcraft.plugins.son.granularity;

import org.workcraft.plugins.son.Interval;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;

public interface TimeGranularity {

	public Integer plus(Integer time, Integer duration) throws TimeOutOfBoundsException;
	public Interval plus(Interval time, Interval duration) throws TimeOutOfBoundsException;

	public Integer subtract(Integer time, Integer duration) throws TimeOutOfBoundsException;
	public Interval subtract(Interval time, Interval duration) throws TimeOutOfBoundsException;

}
