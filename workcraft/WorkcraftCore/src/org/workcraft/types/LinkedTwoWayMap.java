package org.workcraft.types;

import java.util.LinkedHashMap;

public class LinkedTwoWayMap<S, T> extends GeneralTwoWayMap<S, T> {
    public LinkedTwoWayMap() {
        super(new LinkedHashMap<S, T>(), new LinkedHashMap<T, S>());
    }

}
