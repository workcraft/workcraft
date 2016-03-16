package org.workcraft.plugins.son.util;

import java.util.Collection;

public class Interval {

    private Integer min;
    private Integer max;

    public Interval() {
        this.min = 0;
        this.max = 9999;
    }

    public Interval(Integer start, Integer end) {
        this.min = start;
        this.max = end;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    static public Integer getMin(String value) {
        if (value.length() != 9) return null;
        Integer result = 0;

        String first = value.substring(0, 4);

        try {
            //return decimal number
            result = Integer.parseInt(first);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return result;
    }

    static public Integer getMax(String value) {
        if (value.length() != 9) return null;
        Integer result = 9999;

        String last = value.substring(5, 9);

        try {
            //return decimal number
            result = Integer.parseInt(last);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    static public Integer getInteger(String value) {
        if (value.length() != 4) return null;

        Integer result = 0;

        try {
            //return decimal number
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isSpecified() {
        if (getMin() != 0000 || getMax() != 9999) {
            return true;
        }
        return false;
    }

    public boolean isOverlapping(Interval other) {
        if (other == null) return false; // for readability's sake, this condition is pulled out

        // overlap happens ONLY when this's max is on the right of other's min
        // AND this's min is on the left of other's max.
        return ((this.max == null) || (other.min == null) || (this.max.intValue() >= other.min.intValue())) &&
            ((this.min == null) || (other.max == null) || (this.min.intValue() <= other.max.intValue()));
    }

    public static Interval getOverlapping(Collection<Interval> intervals) {
        Interval result = null;

        Interval first = intervals.iterator().next();
        for (Interval interval : intervals) {
            if (first.isOverlapping(interval)) {
                result = new Interval(Math.max(first.getMin(), interval.getMin()), Math.min(first.getMax(), interval.getMax()));
            } else {
                return null;
            }
            first = result;
        }

        return result;
    }

    public static Interval getOverlapping(Interval first, Interval second) {
    	 //System.out.println("first "+first.toString()+ " second "+ second.toString() +" "+ first.isOverlapping(second));
    	 if (first.isOverlapping(second))
    		 return new Interval(Math.max(first.getMin(), second.getMin()), Math.min(first.getMax(), second.getMax()));
    	 else 
    		 return null;
    }

    public boolean isInInterval(Integer number, Interval other) {
        if (number != null && other != null) {
            if (other.getMin() == null && other.getMax() != null) {
                return number.intValue() <= other.getMax().intValue();
            }
            if (other.getMin() != null && other.getMax() == null) {
                return number.intValue() >= other.getMax().intValue();
            }
            if (other.getMin() == null && other.getMax() == null) {
                return true;
            }
            return other.getMin() <= number && number <= other.getMax();
        } else if (number == null && other != null) {
            return other.getMin() == null && other.getMax() == null;
        }
        return false;
    }

    public Interval add(Interval other) {
        int min = getMin() + other.getMin();
        int max = getMax() + other.getMax();

        if (min >= 9999) {
            min = 9999;
        }
        if (max >= 9999) {
            max = 9999;
        }

        return new Interval(min, max);
    }

    public String minToString() {
        return autoComplete(min);
    }

    public String maxToString() {
        return autoComplete(max);
    }

    private String autoComplete(Integer value) {
        String text = value.toString();
        int length = text.length();

        if (length < 4) {
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
    public String toString() {
        return minToString() + "-" + maxToString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Interval) {
            Interval interval = (Interval) obj;
            if (this.getMin().equals(interval.getMin()) && this.getMax().equals(interval.getMax())) {
                return true;
            }
        }
        return false;
    }
}
