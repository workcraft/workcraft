package org.workcraft.gui.graph.tools;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Trace extends ArrayList<String> {

    public static final String EMPTY_TRACE_TEXT = "[empty trace]";

    private int position = 0;

    public Trace() {
        super();
    }

    public Trace(Trace trace) {
        super(trace);
    }

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
        return !isEmpty() && (position < size());
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
        // Position
//        result.append(String.valueOf(getPosition()));
        // Trace
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
                // Position
                try {
                    tmpPosition = Integer.valueOf(s.trim());
                } catch (Exception e) {

                }
                needToParsePosition = false;
            } else {
                // Trace
                for (String st : s.trim().split(",")) {
                    add(st.trim());
                }
            }
        }
        setPosition(tmpPosition);
    }

    public String toText() {
        if (isEmpty()) {
            return EMPTY_TRACE_TEXT;
        }
        return toString();
    }

}
