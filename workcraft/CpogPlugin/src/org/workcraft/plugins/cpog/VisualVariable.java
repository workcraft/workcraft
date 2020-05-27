package org.workcraft.plugins.cpog;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.formula.One;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

@Hotkey(KeyEvent.VK_X)
@DisplayName("Variable")
@SVGIcon("images/cpog-node-variable.svg")
public class VisualVariable extends VisualComponent {
    private static Font variableFont;
    private static Font valueFont;

    private final RenderedFormula valueFalseRenderedFormula = new RenderedFormula(
            VariableState.FALSE.getValueAsString(), One.getInstance(),
            valueFont, Positioning.CENTER, new Point2D.Double(0.0, 0.0));

    private final RenderedFormula valueTrueRenderedFormula = new RenderedFormula(
            VariableState.TRUE.getValueAsString(), One.getInstance(),
            valueFont, Positioning.CENTER, new Point2D.Double(0.0, 0.0));

    private final RenderedFormula valueUndefinedRenderedFormula = new RenderedFormula(
            VariableState.UNDEFINED.getValueAsString(), One.getInstance(),
            valueFont, Positioning.CENTER, new Point2D.Double(0.0, 0.0));

    private RenderedFormula variableRenderedFormula = new RenderedFormula("", One.getInstance(), variableFont, getLabelPositioning(), getLabelOffset());

    static {
        try {
            Font font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb"));
            variableFont = font.deriveFont(0.5f);
            valueFont = font.deriveFont(0.75f);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public VisualVariable(Variable variable) {
        super(variable);

        addPropertyDeclaration(new PropertyDeclaration<>(VariableState.class, Variable.PROPERTY_STATE,
                this::setState, this::getState).setCombinable().setTemplatable());

        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
    }

    @Override
    public Shape getShape() {
        double size = VisualCommonSettings.getNodeSize() - VisualCommonSettings.getStrokeWidth();
        double pos = -0.5 * size;
        return new Rectangle2D.Double(pos, pos, size, size);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        Shape shape = getShape();
        g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setStroke(new BasicStroke((float) VisualCommonSettings.getStrokeWidth()));
        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        g.draw(shape);

        switch (getState()) {
        case FALSE:
            valueFalseRenderedFormula.draw(g);
            break;
        case TRUE:
            valueTrueRenderedFormula.draw(g);
            break;
        case UNDEFINED:
            valueUndefinedRenderedFormula.draw(g);
            break;
        default:
            break;
        }
        drawVariableInLocalSpace(r);
    }

    protected void cacheVariableRenderedFormula(DrawRequest r) {
        if (variableRenderedFormula.isDifferent(getLabel(), One.getInstance(), variableFont, getLabelPositioning(), getLabelOffset())) {
            variableRenderedFormula = new RenderedFormula(getLabel(), One.getInstance(), variableFont, getLabelPositioning(), getLabelOffset());
        }
    }

    protected void drawVariableInLocalSpace(DrawRequest r) {
        if (getLabelVisibility()) {
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            cacheVariableRenderedFormula(r);
            if ((variableRenderedFormula != null) && !variableRenderedFormula.isEmpty()) {
                g.setColor(ColorUtils.colorise(getLabelColor(), d.getColorisation()));
                variableRenderedFormula.draw(g);
            }
        }
    }

    @NoAutoSerialisation
    @Override
    public String getLabel() {
        return getReferencedComponent().getLabel();
    }

    @NoAutoSerialisation
    @Override
    public void setLabel(String label) {
        getReferencedComponent().setLabel(label);
    }

    @Override
    public boolean getLabelVisibility() {
        return true;
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        if (getLabelVisibility() && (variableRenderedFormula != null) && !variableRenderedFormula.isEmpty()) {
            bb = BoundingBoxHelper.union(bb, variableRenderedFormula.getBoundingBox());
        }
        return bb;
    }

    @Override
    public Variable getReferencedComponent() {
        return (Variable) super.getReferencedComponent();
    }

    public VariableState getState() {
        return getReferencedComponent().getState();
    }

    public void setState(VariableState state) {
        getReferencedComponent().setState(state);
    }

    public void toggle() {
        setState(getState().toggle());
    }

}
