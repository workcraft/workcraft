package org.workcraft.dom.math;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.visual.connections.VisualConnection;

@VisualClass(VisualConnection.class)
public class MathConnection extends MathNode implements Connection {

    private MathNode first;
    private MathNode second;

    public MathConnection() {
    }

    public MathConnection(MathNode first, MathNode second) {
        super();
        setDependencies(first, second);
    }

    public void setDependencies(MathNode first, MathNode second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public MathNode getFirst() {
        return first;
    }

    @Override
    public MathNode getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "MathConnection " + this.hashCode() + " (" + first + ", " + second + ")";
    }

}
