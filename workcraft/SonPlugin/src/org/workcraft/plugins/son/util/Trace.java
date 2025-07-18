package org.workcraft.plugins.son.util;

import java.util.ArrayList;

public class Trace extends ArrayList<StepRef> {

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
        return !isEmpty() && (position < size());
    }

    public StepRef getCurrent() {
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
    public StepRef remove(int index) {
        if (index < getPosition()) {
            decPosition(1);
        }
        return super.remove(index);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        // position
        result.append(getPosition());
        result.append(':');
        // trace
        boolean first = true;
        for (StepRef step : this) {
            if (!first) {
                result.append(';');
            }
            result.append(' ');
            result.append(step.toString());
            first = false;
        }
        return result.toString();
    }

    public void fromString(String str) {
        clear();
        int tmpPosition = 0;
        boolean first = true;
        for (String s : str.split(":")) {
            if (first) {
                // position
                try {
                    tmpPosition = Integer.parseInt(s.trim());
                } catch (Exception ignored) {
                }
            } else {
                // trace
                for (String st : s.trim().split(";")) {
                    StepRef step = new StepRef();
                    step.fromString(st);
                    add(step);
                }
            }
            first = false;
        }
        setPosition(tmpPosition);
    }

}
