package org.workcraft.plugins.dtd;

import org.workcraft.plugins.graph.Vertex;

public class SignalEvent extends Vertex {

    Signal getSignal() {
        return (Signal) getParent();
    }

}
