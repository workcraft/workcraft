package org.workcraft.types;

import java.util.LinkedHashMap;

public class LinkedTwoWayMap<T1, T2> extends GeneralTwoWayMap<T1, T2> {
    public LinkedTwoWayMap() {
        super(new LinkedHashMap<T1, T2>(), new LinkedHashMap<T2, T1>());
    }

}
