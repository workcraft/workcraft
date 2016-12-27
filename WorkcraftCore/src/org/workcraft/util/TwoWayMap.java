package org.workcraft.util;

import java.util.HashMap;

public class TwoWayMap<T1, T2> extends GeneralTwoWayMap<T1, T2> {
    public TwoWayMap() {
        super(new HashMap<T1, T2>(), new HashMap<T2, T1>());
    }
}
