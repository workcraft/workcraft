package org.workcraft.plugins.xmas.components;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.xmas.XmasSettings;

public class VisualXmasConnection extends VisualConnection {

	public VisualXmasConnection() {
		super();
	}

	public VisualXmasConnection(MathConnection c) {
		super();
	}

	public VisualXmasConnection(MathConnection con, VisualComponent c1, VisualComponent c2) {
		super(con, c1, c2);
		removePropertyDeclarationByName(VisualConnection.PROPERTY_LINE_WIDTH);
	}

	@Override
	public double getLineWidth() {
		return XmasSettings.getWireWidth();
	}

}
