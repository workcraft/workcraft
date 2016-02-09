package org.workcraft;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Trace extends ArrayList<String> {

    private int position = 0;

    public int getPosition() {
        return position;
    }

    public void setPosition(int value) {
        position = Math.min(Math.max(0, value), size());
    }

    public void incPosition(int value) {
        setPosition(position + value);
    }

    public void decPosition(int value) {
        setPosition(position - value);
    }

    public boolean canProgress() {
        return (!isEmpty() && (position < size()));
    }

    public String getCurrent() {
        return canProgress() ? get(position) : null;
    }

    public void removeCurrent() {
        remove(getPosition());
    }

    @Override
    public void clear() {
        super.clear();
        setPosition(0);
    }

    @Override
    public String remove(int index) {
        if (index < getPosition()) {
            decPosition(1);
        }
        return super.remove(index);
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("");
        // position
//        result.append(String.valueOf(getPosition()));
        //result.append(':');
        // trace
        boolean first = true;
        for (String t : this) {
            if (!first) {
                result.append(", ");
            }
            result.append(t);
            first = false;
        }
        return result.toString();
    }

    public void fromString(String str) {
        clear();
        int tmpPosition = 0;
        boolean needToParsePosition = str.contains(":");
        for (String s : str.split(":")) {
            if (needToParsePosition) {
                // position
                try {
                    tmpPosition = Integer.valueOf(s.trim());
                } catch (Exception e) {

                }
                needToParsePosition = false;
            } else {
                // trace
                for (String st : s.trim().split(",")) {
                    add(st.trim());
                }
            }
        }
        setPosition(tmpPosition);
    }

}
