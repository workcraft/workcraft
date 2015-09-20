package org.workcraft.plugins.son.granularity;

import java.awt.EventQueue;

import org.workcraft.plugins.son.Interval;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.test.test;

public class HourMins implements TimeGranularity{

	@Override
	public Integer plus(Integer time, Integer duration) throws TimeOutOfBoundsException{
		String str = String.valueOf(time);
		Integer hour = 0;
		Integer mins = 0;
		int length;
		switch(length = str.length()){
			case 1:
			case 2: mins = time; break;
			case 3:
				mins = Integer.valueOf(str.substring(1, 3));
				hour = Integer.valueOf(str.substring(0, 1));
				break;
			case 4:
				mins = Integer.valueOf(str.substring(2, 4));
				hour = Integer.valueOf(str.substring(0, 2));
				break;
		}
		System.out.println("hour"+hour);
		System.out.println("mins"+mins);
		return null;
	}

	@Override
	public Interval plus(Interval time, Interval duration) throws TimeOutOfBoundsException{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer subtract(Integer time, Integer duration) throws TimeOutOfBoundsException{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Interval subtract(Interval time, Interval duration) throws TimeOutOfBoundsException{
		// TODO Auto-generated method stub
		return null;
	}

    public static void main(String[] args) {
		HourMins h = new HourMins();
		try {
			h.plus(1, null);
			h.plus(11, null);
			h.plus(245, null);
			h.plus(1432, null);
		} catch (TimeOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
