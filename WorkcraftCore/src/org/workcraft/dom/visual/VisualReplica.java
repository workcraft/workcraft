/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualReplica extends VisualTransformableNode implements Replica, Drawable {
    public static final String PROPERTY_NAME_POSITIONING = "Name positioning";
    public static final String PROPERTY_NAME_COLOR = "Name color";
    public static final String PROPERTY_FOREGROUND_COLOR = "Foreground color";
    public static final String PROPERTY_FILL_COLOR = "Fill color";

    public static final Font nameFont = new Font(Font.SANS_SERIF, Font.ITALIC, 1).deriveFont(0.5f);

    protected double size = CommonVisualSettings.getBaseSize();
    protected double strokeWidth = CommonVisualSettings.getStrokeWidth();
    private Color foregroundColor = CommonVisualSettings.getBorderColor();
    private Color fillColor = CommonVisualSettings.getFillColor();

    private Positioning namePositioning = CommonVisualSettings.getNamePositioning();
    private RenderedText nameRenderedText = new RenderedText("", nameFont, getNamePositioning(), getNameOffset());
    private Color nameColor = CommonVisualSettings.getNameColor();

    private VisualComponent master = null;

    public VisualReplica() {
        this(true, true);
    }

    public VisualReplica(boolean hasColorProperties, boolean hasNameProperties) {
        super();
        if (hasColorProperties) {
            addColorPropertyDeclarations();
        }
        if (hasNameProperties) {
            addNamePropertyDeclarations();
        }

    }

    private void addColorPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualReplica, Color>(
                this, PROPERTY_FOREGROUND_COLOR, Color.class, true, true, true) {
            protected void setter(VisualReplica object, Color value) {
                object.setForegroundColor(value);
            }
            protected Color getter(VisualReplica object) {
                return object.getForegroundColor();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualReplica, Color>(
                this, PROPERTY_FILL_COLOR, Color.class, true, true, true) {
            protected void setter(VisualReplica object, Color value) {
                object.setFillColor(value);
            }
            protected Color getter(VisualReplica object) {
                return object.getFillColor();
            }
        });
    }

    private void addNamePropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualReplica, Positioning>(
                this, PROPERTY_NAME_POSITIONING, Positioning.class, true, true, true) {
            protected void setter(VisualReplica object, Positioning value) {
                object.setNamePositioning(value);
            }
            protected Positioning getter(VisualReplica object) {
                return object.getNamePositioning();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualReplica, Color>(
                this, PROPERTY_NAME_COLOR, Color.class, true, true, true) {
            protected void setter(VisualReplica object, Color value) {
                object.setNameColor(value);
            }
            protected Color getter(VisualReplica object) {
                return object.getNameColor();
            }
        });
    }

    public Color getNameColor() {
        return nameColor;
    }

    public void setNameColor(Color value) {
        nameColor = value;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME_COLOR));
    }

    public Positioning getNamePositioning() {
        return namePositioning;
    }

    public void setNamePositioning(Positioning value) {
        namePositioning = value;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME_POSITIONING));
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color value) {
        foregroundColor = value;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_FOREGROUND_COLOR));
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color value) {
        fillColor = value;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_FILL_COLOR));
    }

    @Override
    public Point2D getCenterInLocalSpace() {
        return new Point2D.Double(0, 0);
    }

    public Point2D getOffset(Positioning positioning) {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        double xOffset = (positioning.xSign<0) ? bb.getMinX() : (positioning.xSign>0) ? bb.getMaxX() : bb.getCenterX();
        double yOffset = (positioning.ySign<0) ? bb.getMinY() : (positioning.ySign>0) ? bb.getMaxY() : bb.getCenterY();
        return new Point2D.Double(xOffset, yOffset);
    }

    public boolean getNameVisibility() {
        return CommonVisualSettings.getNameVisibility();
    }

    public Point2D getNameOffset() {
        return getOffset(getNamePositioning());
    }

    private void cacheNameRenderedText(DrawRequest r) {
        String name = null;
        MathModel mathModel = r.getModel().getMathModel();
        if (getMaster() instanceof VisualComponent) {
            MathNode mathNode = ((VisualComponent)master).getReferencedComponent();
            name = mathModel.getNodeReference(mathNode);
        }

        if (name == null) {
            name = "";
        }

        Point2D offset = getNameOffset();
        if (nameRenderedText.isDifferent(name, nameFont, getNamePositioning(), offset)) {
            nameRenderedText = new RenderedText(name, nameFont, getNamePositioning(), getNameOffset());
        }
    }

    protected void drawNameInLocalSpace(DrawRequest r) {
        if (getNameVisibility() && (nameRenderedText != null) && !nameRenderedText.isEmpty()) {
            cacheNameRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(Coloriser.colorise(getNameColor(), d.getColorisation()));
            nameRenderedText.draw(g);
        }
    }

    // This method is needed for VisualGroup to update the rendered text of its children
    // before they were drawn, which is necessary for computing their bounding boxes
    public void cacheRenderedText(DrawRequest r) {
        cacheNameRenderedText(r);
    }

    /*
     * The internal bounding box does not include the related label and name of the node
     */
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return new Rectangle2D.Double(-size / 2, -size / 2, size, size);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (getNameVisibility()) {
            bb = BoundingBoxHelper.union(bb, getNameBoundingBox());
        }
        return bb;
    }

    public Rectangle2D getNameBoundingBox() {
        if ((nameRenderedText != null) && !nameRenderedText.isEmpty()) {
            return nameRenderedText.getBoundingBox();
        } else {
            return null;
        }
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getInternalBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualReplica) {
            VisualReplica srcReplica = (VisualReplica)src;
            setForegroundColor(srcReplica.getForegroundColor());
            setFillColor(srcReplica.getFillColor());
            setNameColor(srcReplica.getNameColor());
            setNamePositioning(srcReplica.getNamePositioning());
        }
    }

    @Override
    public void rotateClockwise() {
        setNamePositioning(getNamePositioning().rotateClockwise());
        super.rotateClockwise();
    }

    @Override
    public void rotateCounterclockwise() {
        setNamePositioning(getNamePositioning().rotateCounterclockwise());
        super.rotateCounterclockwise();
    }

    @Override
    public void flipHorizontal() {
        setNamePositioning(getNamePositioning().flipHorizontal());
        super.flipHorizontal();
    }

    @Override
    public void flipVertical() {
        setNamePositioning(getNamePositioning().flipVertical());
        super.flipVertical();
    }

    @Override
    public void setMaster(VisualComponent value) {
        if (master != value) {
            if (master != null) {
                master.removeReplica(this);
            }
            master = value;
            if (master != null) {
                master.addReplica(this);
            }
        }
    }

    @Override
    public VisualComponent getMaster() {
        return master;
    }

    @Override
    public void draw(DrawRequest r) {
        drawNameInLocalSpace(r);
    }

}
