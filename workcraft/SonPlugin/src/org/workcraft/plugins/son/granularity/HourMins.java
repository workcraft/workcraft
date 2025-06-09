package org.workcraft.plugins.son.granularity;

import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;

public class HourMins extends AbstractTimeGranularity {

    @Override
    public Integer plusTD(int time, int duration) throws TimeOutOfBoundsException {
        Integer hour = getHour(time);
        Integer mins = getMins(time);

        int addMins = duration + mins;
        hour = floorDiv(addMins, 60) + hour;
        hour %= 24;
        mins = addMins % 60;

        String merge = hour + autoComplete(mins);
        int result = Integer.parseInt(merge);

        if (result == 0) {
            return 2400;
        }
        return result;
    }

    @Override
    public Integer subtractTD(Integer time, Integer duration) throws TimeOutOfBoundsException {
        Integer hour = getHour(time);
        Integer mins = getMins(time);

        int minusHour = floorDiv(duration, 60);
        int minusMins = duration % 60;

        hour -= minusHour;
        mins -= minusMins;

        if (mins < 0) {
            mins += 60;
            hour -= 1;
        }
        if (hour < 0) {
            hour = (24 + hour % 24) % 24;
        }

        String merge = hour + autoComplete(mins);
        int result = Integer.parseInt(merge);

        if (result == 0) {
            return 2400;
        }
        return result;
    }

    @Override
    public Integer subtractTT(Integer start, Integer end)
            throws TimeOutOfBoundsException {
        Integer hour = 0;
        Integer mins = 0;

        Integer startHour = getHour(start);
        Integer startMins = getMins(start);

        Integer endHour = getHour(end);
        Integer endMins = getMins(end);

        if (end >= start) {
            hour = endHour - startHour;
            mins = endMins - startMins;
        } else {
            hour = startHour - endHour;
            mins = startMins - endMins;
        }

        if (mins < 0) {
            mins += 60;
            hour -= 1;
        }

        String merge = hour + autoComplete(mins);
        int result = Integer.parseInt(merge);

        if (end >= start) {
            return result;
        } else {
            return -result;
        }
    }

    public static void validValue(Integer time) throws TimeOutOfBoundsException {
        String str = String.valueOf(time);
        Integer hour = 0;
        Integer mins = 0;

        switch (str.length()) {
            case 1, 2 -> mins = time;
            case 3 -> {
                mins = Integer.valueOf(str.substring(1, 3));
                hour = Integer.valueOf(str.substring(0, 1));
            }
            case 4 -> {
                mins = Integer.valueOf(str.substring(2, 4));
                hour = Integer.valueOf(str.substring(0, 2));
            }
            default -> { }
        }

        if (hour > 23 || mins > 60 || hour < 0 || mins < 0) {
            if (time != 2400 && time != 9999) {
                throw new TimeOutOfBoundsException("Time value out of bounds " + time);
            }
        }
    }

    private Integer getHour(Integer time) throws TimeOutOfBoundsException {
        String str = String.valueOf(time);
        Integer hour = 0;

        switch (str.length()) {
            case 3 -> hour = Integer.valueOf(str.substring(0, 1));
            case 4 -> hour = Integer.valueOf(str.substring(0, 2));
            default -> {
            }
        }

        if (hour > 23 || hour < 0) {
            if (time != 2400 && time != 9999) {
                throw new TimeOutOfBoundsException("Time value out of bounds " + time);
            }
        }

        return hour;
    }

    private Integer getMins(Integer time) throws TimeOutOfBoundsException {
        String str = String.valueOf(time);
        Integer mins = 0;

        switch (str.length()) {
            case 1, 2 -> mins = time;
            case 3 -> mins = Integer.valueOf(str.substring(1, 3));
            case 4 -> mins = Integer.valueOf(str.substring(2, 4));
            default -> { }
        }

        if (mins > 60 || mins < 0) {
            if (time != 2400 && time != 9999) {
                throw new TimeOutOfBoundsException("Time value out of bounds " + time);
            }
        }

        return mins;
    }

    private String autoComplete(Integer value) {
        StringBuilder text = new StringBuilder(value.toString());
        int length = text.length();

        if (length < 2) {
            while (length < 2) {
                text.insert(0, "0");
                length = text.length();
            }
        }
        return text.toString();
    }

    private static int floorDiv(int x, int y) {
        int r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }
}
