package org.workcraft.plugins.son.algorithm;

public class TimeAlg {

	public static Boolean isValidInput(String value){

		//check for length
		if(value.length() != 9)
			return false;

		String first = value.substring(0, 4);
		String mid = value.substring(4, 5);
		String last = value.substring(5, 9);

		//check format xxxx-xxxx
		try {
		   Integer.parseInt(first);
		   Integer.parseInt(last);
		} catch (NumberFormatException e) {
		   return false;
		}

		if(!mid.equals("-"))
			return false;

		if( Integer.parseInt(first) >  Integer.parseInt(last))
			return false;

		return true;
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
}
