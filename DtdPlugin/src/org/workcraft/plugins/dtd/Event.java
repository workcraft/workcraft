package org.workcraft.plugins.dtd;

import org.workcraft.dom.math.MathNode;

public class Event extends MathNode {

    public Signal getSignal() {
        return (Signal) getParent();
    }

}
