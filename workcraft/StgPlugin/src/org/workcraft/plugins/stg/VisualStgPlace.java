package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;

@DisplayName("Place")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/stg-node-place.svg")
public class VisualStgPlace extends VisualPlace {

    public VisualStgPlace(StgPlace place) {
        super(place);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, StgPlace.PROPERTY_MUTEX,
                value -> getReferencedComponent().setMutex(value),
                () -> getReferencedComponent().isMutex())
                .setCombinable());


        addPropertyDeclaration(new PropertyDeclaration<Mutex.Protocol>(
                Mutex.Protocol.class, StgPlace.PROPERTY_MUTEX_PROTOCOL,
                value -> getReferencedComponent().setMutexProtocol(value),
                () -> getReferencedComponent().getMutexProtocol()) {
            @Override
            public boolean isVisible() {
                return getReferencedComponent().isMutex();
            }
        }.setCombinable());
    }

    @Override
    public StgPlace getReferencedComponent() {
        return (StgPlace) super.getReferencedComponent();
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        if (getReferencedComponent().isMutex()) {
            double size = VisualCommonSettings.getNodeSize() + VisualCommonSettings.getStrokeWidth();
            double pos = -0.5 * size;
            Shape shape = new Ellipse2D.Double(pos, pos, size, size);
            g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
            g.fill(shape);
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
            float strokeWidth = (float) VisualCommonSettings.getStrokeWidth() / 2.0f;
            if (getReferencedComponent().getMutexProtocol() == Mutex.Protocol.EARLY) {
                g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                        1.0f, new float[]{0.05f, 0.1f}, 0.0f));
            } else {
                g.setStroke(new BasicStroke(strokeWidth));
            }
            g.draw(shape);
        }
        super.draw(r);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualStgPlace) {
            VisualStgPlace srcPlace = (VisualStgPlace) src;
            getReferencedComponent().setMutex(srcPlace.getReferencedComponent().isMutex());
        }
    }

}
