package org.workcraft.plugins.son;

public class Interval {

	private Integer min;
	private Integer max;

	public Interval(Integer start, Integer end) {
	    this.min = start;
	    this.max = end;
	}

	public Integer getMin() {
	    return min;
	}

	public Integer getMax() {
	    return max;
	}

	static public Integer getMin(String value){
		if(value.length()!=9)return null;
		Integer result = 0000;

		String first = value.substring(0, 4);

		try{
			result = Integer.parseInt(first);
		} catch (NumberFormatException e) {
			  e.printStackTrace();
		}

		return result;
	}

	static public Integer getMax(String value){
		if(value.length()!=9)return null;
		Integer result = 9999;

		String last = value.substring(5, 9);

		try{
			result = Integer.parseInt(last);
		} catch (NumberFormatException e) {
			  e.printStackTrace();
		}
		return result;
	}

	static public Integer getInteger(String value){
		if(value.length()!=4)return null;

		Integer result = 0000;
		String last = value.toString();

		try{
			result = Integer.parseInt(last);
		} catch (NumberFormatException e) {
			  e.printStackTrace();
		}
		return result;
	}

	public boolean isSpecified(){
		if (getMin() != 0000 || getMax() !=9999)
			return true;
		return false;
	}

	public boolean isOverlapping(Interval other) {
	    if (other == null) return false; // for readability's sake, this condition is pulled out

	    // overlap happens ONLY when this's max is on the right of other's min
	    // AND this's min is on the left of other's max.
	    return (((this.max == null) || (other.min == null) || (this.max.intValue() >= other.min.intValue())) &&
	        ((this.min == null) || (other.max == null) || (this.min.intValue() <= other.max.intValue())));
	}

	public boolean isInInterval(Integer number, Interval interval) {
	    if (number != null && interval != null) {
	        if(interval.getMin() == null && interval.getMax() != null) {
	            return number.intValue() <= interval.getMax().intValue();
	        }
	        if(interval.getMin() != null && interval.getMax() == null) {
	            return number.intValue() >= interval.getMax().intValue();
	        }
	        if(interval.getMin() == null && interval.getMax() == null) {
	            return true;
	        }
	        return interval.getMin() <= number && number <= interval.getMax();
	    }
	    else if(number == null && interval != null) {
	        return interval.getMin() == null && interval.getMax() == null;
	    }
	    return false;
	}

	public String minToString(){
		return autoComplete(min);
	}

	public String maxToString(){
		return autoComplete(max);
	}

	private String autoComplete(Integer value){
		String text = value.toString();
		int length = text.length();

		if(length < 4){
		   while (length < 4) {
		    StringBuffer sb = new StringBuffer();
		    sb.append("0").append(text);
		    text = sb.toString();
		    length = text.length();
		   }
		}
		return text;
	}

	@Override
	public String toString(){
		return minToString() + "-" + maxToString();
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Interval){
			Interval interval = (Interval)obj;
			if(this.getMin().equals(interval.getMin()) && this.getMax().equals(interval.getMax()))
				return true;
		}
		return false;
	}
}
