package org.workcraft.plugins.dtd;

import org.workcraft.dom.math.MathNode;

public class SignalEvent extends MathNode {

    public Signal getSignal() {
        return (Signal) getParent();
    }

}
