package org.workcraft.plugins.xmas.components;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.utils.ColorUtils;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.utils.Geometry;

import java.awt.*;
import java.awt.geom.Line2D;

public class VisualXmasConnection extends VisualConnection {

    public VisualXmasConnection() {
        this(null, null, null);
    }

    public VisualXmasConnection(MathConnection con) {
        this(con, null, null);
    }

    public VisualXmasConnection(MathConnection refConnection, VisualNode first, VisualNode second) {
        super(refConnection, first, second);
        removePropertyDeclarationByName(PROPERTY_LINE_WIDTH);
    }

    @Override
    public double getLineWidth() {
        return XmasSettings.getWireWidth();
    }

    @Override
    public void draw(DrawRequest r) {
        // Draw a small pieces of line at the beginning and at the end of connection arc when the contacts are hidden.
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        boolean inSimulationMode = (d.getColorisation() != null) || (d.getBackground() != null);
        Color colorisation = d.getColorisation();
        g.setColor(ColorUtils.colorise(getColor(), colorisation));
        g.setStroke(new BasicStroke((float) XmasSettings.getWireWidth()));

        if (!inSimulationMode && !XmasSettings.getShowContacts()) {
            double tStart = Geometry.getBorderPointParameter(getFirstShape(), getGraphic(), 0, 1);
            g.draw(new Line2D.Double(getFirstCenter(), getGraphic().getPointOnCurve(tStart)));
            double tEnd = Geometry.getBorderPointParameter(getSecondShape(), getGraphic(), 1, 0);
            g.draw(new Line2D.Double(getGraphic().getPointOnCurve(tEnd), getSecondCenter()));
        }
    }

}
