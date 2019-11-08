package org.workcraft.plugins.dfs;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.properties.PropertyDeclaration;

public class VisualControlConnection extends VisualConnection {

    public VisualControlConnection() {
        this(null, null, null);
    }

    public VisualControlConnection(ControlConnection refConnection) {
        this(refConnection, null, null);
    }

    public VisualControlConnection(ControlConnection refConnection, VisualNode first, VisualNode second) {
        super(refConnection, first, second);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        super.initialise();
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, ControlConnection.PROPERTY_INVERTING,
                value -> {
                    ControlConnection ref = getReferencedConnection();
                    // check if ref is not null to trick the order of node creation in deserialiser
                    if (ref != null) {
                        ref.setInverting(value);
                    }
                    setBubble(value);
                },
                () -> getReferencedConnection().isInverting())
                .setCombinable().setTemplatable());
    }

    @Override
    public ControlConnection getReferencedConnection() {
        return (ControlConnection) super.getReferencedConnection();
    }

    @Override
    public void setVisualConnectionDependencies(VisualNode first, VisualNode second,
            ConnectionGraphic graphic, MathConnection refConnection) {
        super.setVisualConnectionDependencies(first, second, graphic, refConnection);
        setBubble(getReferencedConnection().isInverting());
    }

}
