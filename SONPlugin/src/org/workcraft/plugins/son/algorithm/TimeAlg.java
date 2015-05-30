package org.workcraft.plugins.son.algorithm;

import org.workcraft.plugins.son.SON;

public class TimeAlg extends RelationAlgorithm{

	public TimeAlg(SON net) {
		super(net);
	}

	static public Integer getMinTime(String value){
		Integer reslut = 0000;

		String first = value.substring(0, 4);

		try{
			reslut = Integer.parseInt(first);
		} catch (NumberFormatException e) {
			  e.printStackTrace();
		}

		return reslut;
	}

	static public Integer getMaxTime(String value){
		Integer reslut = 9999;

		String last = value.substring(5, 9);

		try{
			reslut = Integer.parseInt(last);
		} catch (NumberFormatException e) {
			  e.printStackTrace();
		}
		return reslut;
	}

	static public boolean isSpecified(String value){
		if (TimeAlg.getMinTime(value) != 0000 || TimeAlg.getMaxTime(value) !=9999)
			return true;
		return false;
	}
}
