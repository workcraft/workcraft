package org.workcraft.plugins.son.granularity;

import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;

public class YearYear extends AbstractTimeGranularity{

	@Override
	public Integer plusTD(int time, int duration) throws TimeOutOfBoundsException {
		int result = time + duration;
//		if(result > 9999)
//			throw new TimeOutOfBoundsException("Time out of bound: time + duration =" +result+".");
		return result;
	}


	@Override
	public Integer subtractTD(Integer time, Integer duration)
			throws TimeOutOfBoundsException {
		int result = time - duration;
//		if(result < 0000)
//			throw new TimeOutOfBoundsException("Time out of bound: time - duration =" +result+".");
		return result;
	}


	@Override
	public Integer subtractTT(Integer start, Integer end)
			throws TimeOutOfBoundsException {
		int result = end - start;
		return result;
	}

}
