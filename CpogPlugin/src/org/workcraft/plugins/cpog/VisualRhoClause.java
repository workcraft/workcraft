package org.workcraft.plugins.cpog;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.FormulaRenderingResult;
import org.workcraft.formula.utils.FormulaToGraphics;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.cpog.formula.PrettifyBooleanReplacer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

@Hotkey(KeyEvent.VK_R)
@DisplayName("RhoClause")
@SVGIcon("images/cpog-node-rho.svg")
public class VisualRhoClause extends VisualComponent {
    private static float strokeWidth = 0.038f;

    private Rectangle2D boudingBox = new Rectangle2D.Float(0, 0, 0, 0);

    private static Font font;

    static {
        try {
            font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb")).deriveFont(0.5f);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public VisualRhoClause(RhoClause rhoClause) {
        super(rhoClause);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
    }

    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Color background = r.getDecoration().getBackground();

        FormulaRenderingResult result = FormulaToGraphics.render(getFormula(), g.getFontRenderContext(), font);

        Rectangle2D textBB = result.boundingBox;

        float textX = (float) -textBB.getCenterX();
        float textY = (float) -textBB.getCenterY();

        float width = (float) textBB.getWidth() + 0.4f;
        float height = (float) textBB.getHeight() + 0.2f;

        boudingBox = new Rectangle2D.Float(-width / 2, -height / 2, width, height);

        g.setStroke(new BasicStroke(strokeWidth));

        g.setColor(Coloriser.colorise(getFillColor(), background));
        g.fill(boudingBox);
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.draw(boudingBox);

        AffineTransform transform = g.getTransform();
        g.translate(textX, textY);
        g.setColor(Coloriser.colorise(getColor(), colorisation));
        result.draw(g);

        g.setTransform(transform);
    }

    private Color getColor() {
        BooleanFormula value = evaluate();
        if (value == One.instance()) {
            return new Color(0x00cc00);
        } else {
            if (value == Zero.instance()) {
                return Color.RED;
            } else {
                return getForegroundColor();
            }
        }
    }

    private BooleanFormula evaluate() {
        return getFormula().accept(new PrettifyBooleanReplacer());
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        return boudingBox;
    }

    public BooleanFormula getFormula() {
        return ((RhoClause) getReferencedComponent()).getFormula();
    }

    public void setFormula(BooleanFormula value) {
        ((RhoClause) getReferencedComponent()).setFormula(value);
    }
}
