package org.workcraft.plugins.parity;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_V)
@DisplayName("Player1")
@SVGIcon("images/parity-node-player1.svg")

/**
 * Visual component subclass that represents the visual portion of vertices
 * owned by Player 1.
 */
public class VisualPlayer1 extends VisualComponent {

    /**
     * RenderType covers the user defined name of a node, and what shape it is.
     */
    public enum RenderType {
        CIRCLE("Circle"),
        SQUARE("Square"),
        LABEL("Label");

        private final String name;

        RenderType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Epsilon symbol in UTF-8 encoding (avoid inserting UTF symbols directly 
     * in the source code).
     */
    public static final String EPSILON_SYMBOL = Character.toString((char) 0x03B5);

    public static final String PROPERTY_RENDER_TYPE = "Render type";
    public static final String PROPERTY_SYMBOL_POSITIONING = "Symbol positioning";
    public static final String PROPERTY_SYMBOL_COLOR = "Symbol color";

    // Player 1 vertices will all be squares.
    private RenderType renderType = RenderType.SQUARE;

    /**
     * Constructor.
     * @param p1    Instance of Player 1
     */
    public VisualPlayer1(Player1 p1) {
        super(p1);
        addPropertyDeclarations();
        removePropertyDeclarationByName(PROPERTY_LABEL);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_COLOR);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        setLabelPositioning(Positioning.CENTER);
    }

    /**
     * Function that allows the addition of Property Declarations. The main
     * subject here is to be able to update the priority of a vertex.
     */
    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Integer.class,
                Player1.PROPERTY_PRIORITY,
                priority -> getReferencedComponent().setPrio(priority),
                () -> getReferencedComponent().getPrio()).setCombinable().setTemplatable());
    }

    /**
     * Get the current Player 1 mathematical component.
     * @return    Corresponding mathematical Player 1 component
     */
    @Override
    public Player1 getReferencedComponent() {
        return (Player1) super.getReferencedComponent();
    }

    /**
     * Get the shape and size of the default vertex for Player 0 when
     * initialised.
     * @return    Shape structure of vertex
     */
    @Override
    public Shape getShape() {
        double size = VisualCommonSettings.getNodeSize() 
            - VisualCommonSettings.getStrokeWidth();
        double pos = -0.5 * size;
        Shape shape = new Rectangle2D.Double(pos, pos, size, size);
        if (getRenderType() != null) {
            switch (getRenderType()) {
            case CIRCLE:
                shape = new Ellipse2D.Double(pos, pos, size, size);
                break;
            case SQUARE:
                shape = new Rectangle2D.Double(pos, pos, size, size);
                break;
            case LABEL:
                shape = new Path2D.Double();
                break;
            default:
                shape = new Ellipse2D.Double(pos, pos, size, size);
                break;
            }
        }
        return shape;
    }

    /**
     * Predicate function to check if two shapes are overlapping.
     * @param pointInLocalSpace    2D point to check
     * @return                     true if a point is being occupied by
     *                             two different objects
     */
    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        if (getRenderType() == RenderType.LABEL) {
            return getLabelBoundingBox().contains(pointInLocalSpace);
        }
        return getShape().contains(pointInLocalSpace);
    }

    /**
     * Get the renderType of this visual vertex.
     * @return renderType
     */
    public RenderType getRenderType() {
        return renderType;
    }

    /**
     * Set the renderType of this visual vertex.
     * @param value    The new renderType for this vertex
     */
    public void setRenderType(RenderType value) {
        if (renderType != value) {
            renderType = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_RENDER_TYPE));
        }
    }

    /**
     * Get the label visibility (name visibility) of vertices.
     * @return Always returns true.
     */
    @Override
    public boolean getLabelVisibility() {
        return true;
    }

    /**
     * Overridden function that will be used to cache the priority of the
     * current vertex
     * @param r    Current DrawRequest
     */
    @Override
    protected void cacheLabelRenderedText(DrawRequest r) {
        Symbol symbol = getReferencedComponent().getSymbol();
        Player1 tempNode = getReferencedComponent();
        if (symbol != null) {

            String label = Integer.toString(tempNode.getPrio());
            cacheLabelRenderedText(label, getLabelFont(), Positioning.CENTER,
                    getLabelOffset());
        }
    }

    /**
     * Overridden function to draw the priority value in the middle of the shape.
     * @param r    Current DrawRequest
     */
    @Override
    protected void drawLabelInLocalSpace(DrawRequest r) {
        if (getLabelVisibility()) {
            cacheLabelRenderedText(r);
            Player1 tempNode = getReferencedComponent();
            String label = Integer.toString(tempNode.getPrio());
            RenderedText displayText = new RenderedText(label, getLabelFont(),
                    Positioning.CENTER, getLabelOffset());
            Graphics2D g = r.getGraphics();
            displayText.draw(g);
        }
    }

    /**
     * Copy the style of some Visual Player 0 node.
     * @param src    A Stylable object to be copied recursively
     */
    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualPlayer1) {
            VisualPlayer1 srcComponent = (VisualPlayer1) src;
            setRenderType(srcComponent.getRenderType());
        }
    }

}