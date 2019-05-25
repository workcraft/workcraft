package org.workcraft.types;

import java.util.HashMap;

public class TwoWayMap<T1, T2> extends GeneralTwoWayMap<T1, T2> {
    public TwoWayMap() {
        super(new HashMap<>(), new HashMap<>());
    }
}
