package org.workcraft.types;

import java.util.HashMap;

public class TwoWayMap<S, T> extends GeneralTwoWayMap<S, T> {
    public TwoWayMap() {
        super(new HashMap<>(), new HashMap<>());
    }
}
