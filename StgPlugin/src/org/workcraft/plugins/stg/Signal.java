package org.workcraft.plugins.stg;

import org.workcraft.plugins.stg.SignalTransition.Type;

public class Signal {

    public final String name;
    public final Type type;

    public Signal(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public int hashCode() {
        return (name.hashCode() << 1) + type.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Signal) {
            Signal s = (Signal) o;
            result = name.equals(s.name) && type.equals(s.type);
        }
        return result;
    }

}
