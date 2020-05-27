package org.workcraft.plugins.petri;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.petri.tools.PlaceDecoration;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

@DisplayName("Place")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/petri-node-place.svg")
public class VisualPlace extends VisualComponent {

    public static final String PROPERTY_TOKEN_COLOR = "Token color";

    protected static double singleTokenSize = VisualCommonSettings.getNodeSize() / 1.9;
    protected static double multipleTokenSeparation = VisualCommonSettings.getStrokeWidth() / 8;
    protected Color tokenColor = VisualCommonSettings.getBorderColor();

    public VisualPlace(Place place) {
        super(place);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Integer.class, Place.PROPERTY_TOKENS,
                value -> getReferencedComponent().setTokens(value),
                () -> getReferencedComponent().getTokens())
                .setCombinable());

        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_TOKEN_COLOR,
                this::setTokenColor, this::getTokenColor).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Integer.class, Place.PROPERTY_CAPACITY,
                value -> getReferencedComponent().setCapacity(value),
                () -> getReferencedComponent().getCapacity())
                .setCombinable().setTemplatable());
    }

    @Override
    public boolean getNameVisibility() {
        return super.getNameVisibility() || !getReplicas().isEmpty();
    }

    @Override
    public Shape getShape() {
        double size = VisualCommonSettings.getNodeSize() - VisualCommonSettings.getStrokeWidth();
        double pos = -0.5 * size;
        return new Ellipse2D.Double(pos, pos, size, size);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        double size = VisualCommonSettings.getNodeSize() - VisualCommonSettings.getStrokeWidth();
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Decoration d = r.getDecoration();
        Place place = getReferencedComponent();
        int tokenCount = place.getTokens();
        Color tokenColor = getTokenColor();
        if (d instanceof PlaceDecoration) {
            tokenCount = ((PlaceDecoration) d).getTokens();
            tokenColor = ((PlaceDecoration) d).getTokenColor();
        }
        double tokenSpace = VisualCommonSettings.getNodeSize() - 2.0 * VisualCommonSettings.getStrokeWidth();
        drawTokens(r, tokenCount, singleTokenSize, multipleTokenSeparation, tokenSpace, tokenColor);
    }

    @Override
    public Place getReferencedComponent() {
        return (Place) super.getReferencedComponent();
    }

    public Color getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(Color value) {
        if (!tokenColor.equals(value)) {
            tokenColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TOKEN_COLOR));
        }
    }

    public static void drawTokens(DrawRequest r, int count, double size, double separation, double space, Color color) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        Shape shape;
        if (count == 1) {
            shape = new Ellipse2D.Double(-size / 2, -size / 2, size, size);
            g.setColor(ColorUtils.colorise(color, d.getColorisation()));
            g.fill(shape);
        } else {
            if (count > 1 && count < 8) {
                double alpha = Math.PI / count;
                if (count == 7) alpha = Math.PI / 6;
                double radius = (space / 2 - separation) / (1 + 1 / Math.sin(alpha));
                double step = radius / Math.sin(alpha);
                radius -= separation;
                for (int i = 0; i < count; i++) {
                    if (i == 6) {
                        shape = new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2);
                    } else {
                        shape = new Ellipse2D.Double(
                                -step * Math.sin(i * alpha * 2) - radius,
                                -step * Math.cos(i * alpha * 2) - radius,
                                radius * 2, radius * 2);
                    }
                    g.setColor(ColorUtils.colorise(color, d.getColorisation()));
                    g.fill(shape);
                }
            } else if (count > 7) {
                String tokenString = Integer.toString(count);
                Font superFont = g.getFont().deriveFont((float) VisualCommonSettings.getNodeSize() / 2);
                Rectangle2D rect = superFont.getStringBounds(tokenString, g.getFontRenderContext());
                g.setFont(superFont);
                g.setColor(ColorUtils.colorise(color, d.getColorisation()));
                g.drawString(tokenString, (float) (-rect.getCenterX()), (float) (-rect.getCenterY()));
            }
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualPlace) {
            VisualPlace srcPlace = (VisualPlace) src;
            getReferencedComponent().setCapacity(srcPlace.getReferencedComponent().getCapacity());
            getReferencedComponent().setTokens(srcPlace.getReferencedComponent().getTokens());
            setTokenColor(srcPlace.getTokenColor());
        }
    }

    @Override
    public void mixStyle(Stylable... srcs) {
        super.mixStyle(srcs);
        int tokens = 0;
        int capacity = 0;
        LinkedList<Color> tokenColors = new LinkedList<>();
        for (Stylable src: srcs) {
            if (src instanceof VisualPlace) {
                VisualPlace srcPlace = (VisualPlace) src;
                int tmpTokens = srcPlace.getReferencedComponent().getTokens();
                if (tokens < tmpTokens) {
                    tokens = tmpTokens;
                }
                int tmpCapacity = srcPlace.getReferencedComponent().getCapacity();
                if (capacity < tmpCapacity) {
                    capacity = tmpCapacity;
                }
                tokenColors.add(srcPlace.getTokenColor());
            }
        }
        getReferencedComponent().setTokens(tokens);
        getReferencedComponent().setCapacity(capacity);
        setTokenColor(ColorUtils.mix(tokenColors));
    }

}
