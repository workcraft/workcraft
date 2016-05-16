package org.workcraft.plugins.dtd;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;

public class VisualLevelConnection extends VisualConnection {

    public VisualLevelConnection() {
        this(null, null, null);
    }

    public VisualLevelConnection(MathConnection mathConnection) {
        this(mathConnection, null, null);
    }

    public VisualLevelConnection(MathConnection mathConnection, VisualNode first, VisualNode second) {
        super(mathConnection, first, second);
        removePropertyDeclarationByName(PROPERTY_ARROW_LENGTH);
        removePropertyDeclarationByName(PROPERTY_ARROW_WIDTH);
        removePropertyDeclarationByName(PROPERTY_CONNECTION_TYPE);
        removePropertyDeclarationByName(PROPERTY_LINE_WIDTH);
        removePropertyDeclarationByName(PROPERTY_SCALE_MODE);
    }

}
