package org.workcraft.traces;

import org.workcraft.utils.TraceUtils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Trace extends ArrayList<String> {

    private static final String EMPTY_TEXT = "[empty trace]";

    private int position = 0;

    public Trace() {
        super();
    }

    public Trace(Trace trace) {
        super(trace);
        setPosition(trace.getPosition());
    }

    public int getPosition() {
        return adjustPosition(position);
    }

    public void setPosition(int value) {
        position = adjustPosition(value);
    }

    private int adjustPosition(int value) {
        return Math.min(Math.max(0, value), size());
    }

    public void incPosition() {
        setPosition(position + 1);
    }

    public void decPosition() {
        setPosition(position - 1);
    }

    public boolean canProgress() {
        return !isEmpty() && (getPosition() < size());
    }

    public String getCurrent() {
        return canProgress() ? get(getPosition()) : null;
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
            decPosition();
        }
        return super.remove(index);
    }

    @Override
    public String toString() {
        return isEmpty() ? EMPTY_TEXT : TraceUtils.serialiseTrace(this);
    }

}
