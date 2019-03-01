package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.circuit.tools.StateDecoration;
import org.workcraft.utils.Geometry;

public class VisualCircuitConnection extends VisualConnection {

    public VisualCircuitConnection() {
        this(null, null, null);
    }

    public VisualCircuitConnection(MathConnection refConnection) {
        this(refConnection, null, null);
    }

    public VisualCircuitConnection(MathConnection refConnection, VisualComponent c1, VisualComponent c2) {
        super(refConnection, c1, c2);
        removePropertyDeclarationByName(PROPERTY_LINE_WIDTH);
    }

    @Override
    public double getLineWidth() {
        return CircuitSettings.getWireWidth();
    }

    @Override
    public void draw(DrawRequest r) {
        // Draw a small pieces of line at the beginning and at the end of connection arc when the gate contacts are hidden.
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        Color colorisation = d.getColorisation();
        g.setColor(Coloriser.colorise(getColor(), colorisation));
        g.setStroke(new BasicStroke((float) CircuitSettings.getWireWidth()));

        boolean showContact = CircuitSettings.getShowContacts() || (d instanceof StateDecoration);

        if (!showContact && (getFirst().getParent() instanceof VisualCircuitComponent)) {
            double tStart = Geometry.getBorderPointParameter(getFirstShape(), getGraphic(), 0, 1);
            g.draw(new Line2D.Double(getFirstCenter(), getGraphic().getPointOnCurve(tStart)));
        }

        if (!showContact && (getSecond().getParent() instanceof VisualCircuitComponent)) {
            double tEnd = Geometry.getBorderPointParameter(getSecondShape(), getGraphic(), 1, 0);
            g.draw(new Line2D.Double(getGraphic().getPointOnCurve(tEnd), getSecondCenter()));
        }
    }

}
