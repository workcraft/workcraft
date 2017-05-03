package org.workcraft.plugins.stg;

import org.workcraft.plugins.stg.SignalTransition.Type;

public class Signal {

    public final String name;
    public final Type type;

    public Signal(String name, Type type) {
        this.name = name;
        this.type = type;
    }

}
