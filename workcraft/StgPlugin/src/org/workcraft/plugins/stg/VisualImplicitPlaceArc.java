package org.workcraft.plugins.stg;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.tools.PlaceDecoration;
import org.workcraft.serialisation.NoAutoSerialisation;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class VisualImplicitPlaceArc extends VisualConnection {

    private static final double TOKEN_SIZE = VisualCommonSettings.getNodeSize() / 1.9;
    private static final double TOKEN_SEPARATION = VisualCommonSettings.getStrokeWidth() / 8;
    private static final double TOKEN_SPACE = VisualCommonSettings.getNodeSize() - 2.0 * VisualCommonSettings.getStrokeWidth();

    private StgPlace implicitPlace;
    private MathConnection refCon1;
    private MathConnection refCon2;
    private Color tokenColor = VisualCommonSettings.getBorderColor();

    public VisualImplicitPlaceArc() {
        super();
        addPropertyDeclarations();
    }

    public VisualImplicitPlaceArc(VisualComponent first, VisualComponent second,
            MathConnection refCon1, MathConnection refCon2, StgPlace implicitPlace) {
        super(null, first, second);
        this.refCon1 = refCon1;
        this.refCon2 = refCon2;
        this.implicitPlace = implicitPlace;

        addPlaceObserver(implicitPlace);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Integer.class, Place.PROPERTY_TOKENS,
                value -> getImplicitPlace().setTokens(value),
                () -> getImplicitPlace().getTokens())
                .setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Integer.class, Place.PROPERTY_CAPACITY,
                value -> getImplicitPlace().setCapacity(value),
                () -> getImplicitPlace().getCapacity())
                .setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, VisualPlace.PROPERTY_TOKEN_COLOR,
                this::setTokenColor, this::getTokenColor).setCombinable().setTemplatable());
    }

    private void addPlaceObserver(Place implicitPlace) {
        implicitPlace.addObserver(observableStateImpl::sendNotification);
    }

    public void setImplicitPlaceArcDependencies(MathConnection refCon1, MathConnection refCon2, StgPlace implicitPlace) {
        this.refCon1 = refCon1;
        this.refCon2 = refCon2;
        this.implicitPlace = implicitPlace;

        addPlaceObserver(implicitPlace);
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Decoration d = r.getDecoration();
        int tokenCount = implicitPlace.getTokens();
        Color tokenColor = getTokenColor();
        if (d instanceof PlaceDecoration) {
            tokenCount = ((PlaceDecoration) d).getTokens();
            tokenColor = ((PlaceDecoration) d).getTokenColor();
        }

        Point2D p = getMiddleSegmentCenterPoint();
        Graphics2D g = r.getGraphics();
        g.translate(p.getX(), p.getY());
        VisualPlace.drawTokens(r, tokenCount, TOKEN_SIZE, TOKEN_SEPARATION, TOKEN_SPACE, tokenColor);
    }

    @NoAutoSerialisation
    public StgPlace getImplicitPlace() {
        return implicitPlace;
    }

    public MathConnection getRefCon1() {
        return refCon1;
    }

    public MathConnection getRefCon2() {
        return refCon2;
    }

    @Override
    public Set<MathNode> getMathReferences() {
        Set<MathNode> ret = new HashSet<>();
        ret.add(implicitPlace);
        ret.add(refCon1);
        ret.add(refCon2);
        return ret;
    }

    public Color getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(Color value) {
        if (!tokenColor.equals(value)) {
            tokenColor = value;
            sendNotification(new PropertyChangedEvent(this, VisualPlace.PROPERTY_TOKEN_COLOR));
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualImplicitPlaceArc srcImplicitPlaceArc) {
            setTokenColor(srcImplicitPlaceArc.getTokenColor());
            StgPlace srcPlace = srcImplicitPlaceArc.getImplicitPlace();
            getImplicitPlace().setTokens(srcPlace.getTokens());
            getImplicitPlace().setCapacity(srcPlace.getCapacity());
        }
    }
}
