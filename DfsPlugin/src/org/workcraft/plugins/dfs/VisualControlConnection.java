package org.workcraft.plugins.dfs;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

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
        addPropertyDeclaration(new PropertyDeclaration<VisualControlConnection, Boolean>(
                this, ControlConnection.PROPERTY_INVERTING, Boolean.class, true, true, true) {
            public void setter(VisualControlConnection object, Boolean value) {
                ControlConnection ref = getReferencedControlConnection();
                // check if ref is not null to trick the order of node creation in deserialiser
                if (ref != null) {
                    ref.setInverting(value);
                }
                setBubble(value);
            }
            public Boolean getter(VisualControlConnection object) {
                return object.getReferencedControlConnection().isInverting();
            }
        });
    }

    public     ControlConnection getReferencedControlConnection() {
        return (ControlConnection)getReferencedConnection();
    }

    @Override
    public void setVisualConnectionDependencies(VisualNode first, VisualNode second,
            ConnectionGraphic graphic, MathConnection refConnection) {
        super.setVisualConnectionDependencies(first, second, graphic, refConnection);
        setBubble(getReferencedControlConnection().isInverting());
    }

}
