package org.workcraft.plugins.son.util;

import java.util.ArrayList;

public class ScenarioSaveList extends ArrayList<ScenarioRef>{

    private static final long serialVersionUID = 1L;

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

}
