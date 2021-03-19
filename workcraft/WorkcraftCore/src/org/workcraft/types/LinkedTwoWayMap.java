package org.workcraft.types;

import java.util.LinkedHashMap;

public class LinkedTwoWayMap<S, T> extends GeneralTwoWayMap<S, T> {
    public LinkedTwoWayMap() {
        super(new LinkedHashMap<>(), new LinkedHashMap<>());
    }
}
