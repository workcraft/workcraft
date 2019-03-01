package org.workcraft.formula.utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.formula.And;
import org.workcraft.formula.BinaryBooleanFormula;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Iff;
import org.workcraft.formula.Imply;
import org.workcraft.formula.Not;
import org.workcraft.formula.One;
import org.workcraft.formula.Or;
import org.workcraft.formula.Xor;
import org.workcraft.formula.Zero;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;

public class FormulaToGraphics {

    public final class Void {
        private Void() { }
    }

    public static Font defaultFont;
    public static Font defaultSubFont;

    static {
        try {
            defaultFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb")).deriveFont(0.5f);

            Map<TextAttribute, Integer> attributes = new HashMap<>();
            attributes.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
            defaultSubFont = defaultFont.deriveFont(attributes);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public static FormulaRenderingResult print(String text, Font font, FontRenderContext fontRenderContext) {
        if (text.length() < 1) text = " ";

        Map<TextAttribute, Integer> attributes = new HashMap<>();
        attributes.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
        Font subfont = font.deriveFont(attributes);

        float fontSize = font.getSize2D();
        FormulaRenderingResult res = print(text.charAt(0), font, fontRenderContext);
        if (!font.canDisplay(text.charAt(0))) {
            res = print(text.charAt(0), defaultFont.deriveFont(fontSize), fontRenderContext);
        }

        int subIndex = CommonVisualSettings.getUseSubscript() ? text.lastIndexOf('_') : -1;
        if (subIndex < 0) subIndex = text.length();

        for (int i = 1; i < text.length(); i++) {
            if (i == subIndex) continue;

            if (i < subIndex) {
                if (font.canDisplay(text.charAt(i))) {
                    res.add(print(text.charAt(i), font, fontRenderContext));
                } else {
                    res.add(print(text.charAt(i), defaultFont.deriveFont(fontSize), fontRenderContext));
                }
            } else {
                if (subfont.canDisplay(text.charAt(i))) {
                    res.add(print(text.charAt(i), subfont, fontRenderContext));
                } else {
                    res.add(print(text.charAt(i), defaultSubFont.deriveFont(fontSize), fontRenderContext));
                }
            }
        }
        return res;
    }

    public static FormulaRenderingResult print(char c, Font font, FontRenderContext fontRenderContext) {
        FormulaRenderingResult result = new FormulaRenderingResult();

        GlyphVector glyphs;

        glyphs = font.createGlyphVector(fontRenderContext, "" + c);

        result.boundingBox = glyphs.getVisualBounds();
        result.boundingBox.add(new Point2D.Double(
                glyphs.getLogicalBounds().getMaxX(),
                glyphs.getLogicalBounds().getCenterY()));

        result.visualTop = glyphs.getVisualBounds().getMinY();
        result.glyphs = new ArrayList<GlyphVector>();
        result.glyphs.add(glyphs);
        result.glyphCoordinates = new ArrayList<Point2D>();
        result.glyphCoordinates.add(new Point2D.Double(0, 0));
        result.inversionLines = new ArrayList<Line2D>();

        return result;
    }

    public static class PrinterSuite {
        public PrinterSuite() {
            iff = new IffPrinter();
            imply = new ImplyPrinter();
            or = new OrPrinter();
            xor = new XorPrinter();
            and = new AndPrinter();
            not = new NotPrinter(iff);
            constants = new ConstantPrinter();
            vars = new VariablePrinter();
            paren = new ParenthesesPrinter();
        }

        public void init(FontRenderContext fontRenderContext, Font font, boolean unicodeAllowed) {
            init(iff, imply, fontRenderContext, font, unicodeAllowed);
            init(imply, or, fontRenderContext, font, unicodeAllowed);
            init(or, xor, fontRenderContext, font, unicodeAllowed);
            init(xor, and, fontRenderContext, font, unicodeAllowed);
            init(and, not, fontRenderContext, font, unicodeAllowed);
            init(not, vars, fontRenderContext, font, unicodeAllowed);
            init(vars, constants, fontRenderContext, font, unicodeAllowed);
            init(constants, paren, fontRenderContext, font, unicodeAllowed);
            init(paren, iff, fontRenderContext, font, unicodeAllowed);
        }

        public void init(DelegatingPrinter printer, DelegatingPrinter next, FontRenderContext fontRenderContext, Font font, boolean unicodeAllowed) {
            printer.setNext(next);
            printer.setFontRenderContext(fontRenderContext);
            printer.setFont(font);
            printer.unicodeAllowed = unicodeAllowed;
        }

        public IffPrinter iff;
        public ImplyPrinter imply;
        public OrPrinter or;
        public XorPrinter xor;
        public AndPrinter and;
        public NotPrinter not;
        public ConstantPrinter constants;
        public VariablePrinter vars;
        public ParenthesesPrinter paren;
    }

    public static class DelegatingPrinter implements BooleanVisitor<FormulaRenderingResult> {
        public DelegatingPrinter next;
        public FontRenderContext fontRenderContext;
        public Font font;
        public boolean unicodeAllowed = false;

        public void setFontRenderContext(FontRenderContext fontRenderContext) {
            this.fontRenderContext = fontRenderContext;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public void setNext(DelegatingPrinter next) {
            this.next = next;
        }

        public FormulaRenderingResult print(String text) {
            return FormulaToGraphics.print(text, font, fontRenderContext);
        }

        protected FormulaRenderingResult visitBinary(DelegatingPrinter printer, String opSymbol, BinaryBooleanFormula node) {
            FormulaRenderingResult res = node.getX().accept(printer);
            res.add(print(opSymbol));
            res.add(node.getY().accept(printer));
            return res;
        }

        @Override
        public FormulaRenderingResult visit(Zero node) {
            return next.visit(node);
        }

        @Override
        public FormulaRenderingResult visit(One node) {
            return next.visit(node);
        }

        @Override
        public FormulaRenderingResult visit(BooleanVariable node) {
            return next.visit(node);
        }

        @Override
        public FormulaRenderingResult visit(Not node) {
            return next.visit(node);
        }

        @Override
        public FormulaRenderingResult visit(And node) {
            return next.visit(node);
        }

        @Override
        public FormulaRenderingResult visit(Or node) {
            return next.visit(node);
        }

        @Override
        public FormulaRenderingResult visit(Xor node) {
            return next.visit(node);
        }

        @Override
        public FormulaRenderingResult visit(Imply node) {
            return next.visit(node);
        }

        @Override
        public FormulaRenderingResult visit(Iff node) {
            return next.visit(node);
        }
    }

    public static class ConstantPrinter extends DelegatingPrinter {
        @Override
        public FormulaRenderingResult visit(One one) {
            return print("1");
        }

        @Override
        public FormulaRenderingResult visit(Zero zero) {
            return print("0");
        }
    }

    public static class VariablePrinter extends DelegatingPrinter {
        Map<String, BooleanVariable> varMap = new HashMap<>();
        @Override
        public FormulaRenderingResult visit(BooleanVariable var) {
            String label = var.getLabel();
            BooleanVariable nameHolder = varMap.get(label);
            if (nameHolder == null) {
                varMap.put(label, var);
            } else {
                if (nameHolder != var) {
                    throw new RuntimeException("name conflict! duplicate name " + label);
                }
            }
            return print(label);
        }
    }

    public static class NotPrinter extends DelegatingPrinter {
        private final IffPrinter iff;

        public NotPrinter(IffPrinter iff) {
            this.iff = iff;
        }

        @Override
        public FormulaRenderingResult visit(Not node) {
            FormulaRenderingResult res = node.getX().accept(iff);
            res.visualTop -= font.getSize2D() / 8.0;
            double xMin = res.boundingBox.getMinX();
            double xMax = res.boundingBox.getMaxX();
            res.inversionLines.add(new Line2D.Double(xMin, res.visualTop, xMax, res.visualTop));
            double y = res.boundingBox.getMinY() - font.getSize2D() / 8.0;
            res.boundingBox.add(new Point2D.Double(xMax, y));
            return res;
        }
    }

    public static class AndPrinter extends DelegatingPrinter {
        @Override
        public FormulaRenderingResult visit(And node) {
            return visitBinary(this, unicodeAllowed ? "\u00b7" : "*", node);
        }
    }

    public static class OrPrinter extends DelegatingPrinter {
        @Override
        public FormulaRenderingResult visit(Or node) {
            return visitBinary(this, " + ", node);
        }
    }

    public static class XorPrinter extends DelegatingPrinter {
        @Override
        public FormulaRenderingResult visit(Xor node) {
            return visitBinary(this, unicodeAllowed ? " \u2295 " : " ^ ", node);
        }
    }

    public static class ImplyPrinter extends DelegatingPrinter {
        @Override
        public FormulaRenderingResult visit(Imply node) {
            return visitBinary(next, unicodeAllowed ? " \u21d2 " : " => ", node);
        }
    }

    public static class IffPrinter extends DelegatingPrinter {
        @Override
        public FormulaRenderingResult visit(Iff node) {
            return visitBinary(this, " = ", node);
        }
    }

    public static class ParenthesesPrinter extends DelegatingPrinter {
        @Override
        public FormulaRenderingResult visit(Zero node) {
            return enclose(node);
        }

        @Override
        public FormulaRenderingResult visit(One node) {
            return enclose(node);
        }

        @Override
        public FormulaRenderingResult visit(BooleanVariable node) {
            return enclose(node);
        }

        @Override
        public FormulaRenderingResult visit(And node) {
            return enclose(node);
        }

        @Override
        public FormulaRenderingResult visit(Or node) {
            return enclose(node);
        }

        @Override
        public FormulaRenderingResult visit(Xor node) {
            return enclose(node);
        }

        @Override
        public FormulaRenderingResult visit(Iff node) {
            return enclose(node);
        }

        @Override
        public FormulaRenderingResult visit(Imply node) {
            return enclose(node);
        }

        FormulaRenderingResult enclose(BooleanFormula node) {
            FormulaRenderingResult res = print("(");
            res.add(node.accept(next));
            res.add(print(")"));
            return res;
        }
    }

    public static FormulaRenderingResult render(BooleanFormula formula, FontRenderContext fontRenderContext, Font font) {
        return render(formula, fontRenderContext, font, true);
    }

    public static FormulaRenderingResult render(BooleanFormula formula, FontRenderContext fontRenderContext, Font font, boolean unicodeAllowed) {
        PrinterSuite ps = new PrinterSuite();
        ps.init(fontRenderContext, font, unicodeAllowed);
        return formula.accept(ps.iff);
    }
}
