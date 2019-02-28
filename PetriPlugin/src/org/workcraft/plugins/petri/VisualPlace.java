package org.workcraft.plugins.petri;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.petri.tools.PlaceDecoration;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;

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

    protected static double singleTokenSize = CommonVisualSettings.getNodeSize() / 1.9;
    protected static double multipleTokenSeparation = CommonVisualSettings.getStrokeWidth() / 8;
    protected Color tokenColor = CommonVisualSettings.getBorderColor();

    public VisualPlace(Place place) {
        super(place);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualPlace, Integer>(
                this, Place.PROPERTY_TOKENS, Integer.class, true, false) {
            @Override
            public void setter(VisualPlace object, Integer value) {
                object.getReferencedPlace().setTokens(value);
            }
            @Override
            public Integer getter(VisualPlace object) {
                return object.getReferencedPlace().getTokens();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualPlace, Color>(
                this, PROPERTY_TOKEN_COLOR, Color.class, true, true) {
            @Override
            public void setter(VisualPlace object, Color value) {
                object.setTokenColor(value);
            }
            @Override
            public Color getter(VisualPlace object) {
                return object.getTokenColor();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualPlace, Integer>(
                this, Place.PROPERTY_CAPACITY, Integer.class, true, true) {
            @Override
            public void setter(VisualPlace object, Integer value) {
                object.getReferencedPlace().setCapacity(value);
            }
            @Override
            public Integer getter(VisualPlace object) {
                return object.getReferencedPlace().getCapacity();
            }
        });
    }

    @Override
    public boolean getNameVisibility() {
        return super.getNameVisibility() || !getReplicas().isEmpty();
    }

    @Override
    public Shape getShape() {
        double size = CommonVisualSettings.getNodeSize() - CommonVisualSettings.getStrokeWidth();
        double pos = -0.5 * size;
        return new Ellipse2D.Double(pos, pos, size, size);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        double size = CommonVisualSettings.getNodeSize() - CommonVisualSettings.getStrokeWidth();
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Decoration d = r.getDecoration();
        Place place = (Place) getReferencedComponent();
        int tokenCount = place.getTokens();
        Color tokenColor = getTokenColor();
        if (d instanceof PlaceDecoration) {
            tokenCount = ((PlaceDecoration) d).getTokens();
            tokenColor = ((PlaceDecoration) d).getTokenColor();
        }
        double tokenSpace = CommonVisualSettings.getNodeSize() - 2.0 * CommonVisualSettings.getStrokeWidth();
        drawTokens(r, tokenCount, singleTokenSize, multipleTokenSeparation, tokenSpace, tokenColor);
    }

    public Place getReferencedPlace() {
        return (Place) getReferencedComponent();
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
            g.setColor(Coloriser.colorise(color, d.getColorisation()));
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
                    g.setColor(Coloriser.colorise(color, d.getColorisation()));
                    g.fill(shape);
                }
            } else if (count > 7) {
                String tokenString = Integer.toString(count);
                Font superFont = g.getFont().deriveFont((float) CommonVisualSettings.getNodeSize() / 2);
                Rectangle2D rect = superFont.getStringBounds(tokenString, g.getFontRenderContext());
                g.setFont(superFont);
                g.setColor(Coloriser.colorise(color, d.getColorisation()));
                g.drawString(tokenString, (float) (-rect.getCenterX()), (float) (-rect.getCenterY()));
            }
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualPlace) {
            VisualPlace srcPlace = (VisualPlace) src;
            getReferencedPlace().setCapacity(srcPlace.getReferencedPlace().getCapacity());
            getReferencedPlace().setTokens(srcPlace.getReferencedPlace().getTokens());
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
                int tmpTokens = srcPlace.getReferencedPlace().getTokens();
                if (tokens < tmpTokens) {
                    tokens = tmpTokens;
                }
                int tmpCapacity = srcPlace.getReferencedPlace().getCapacity();
                if (capacity < tmpCapacity) {
                    capacity = tmpCapacity;
                }
                tokenColors.add(srcPlace.getTokenColor());
            }
        }
        getReferencedPlace().setTokens(tokens);
        getReferencedPlace().setCapacity(capacity);
        setTokenColor(Coloriser.mix(tokenColors));
    }

}
