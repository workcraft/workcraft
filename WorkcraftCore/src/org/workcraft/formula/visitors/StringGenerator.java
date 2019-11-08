package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

import java.util.HashMap;
import java.util.Map;

public class StringGenerator implements BooleanVisitor<String> {

    public enum Void {
    }

    public enum Style {
        DEFAULT, UNICODE, VERILOG, REACH
    };

    public static class PrinterSuite {
        public PrinterSuite() {
            iff = new IffPrinter();
            imply = new ImplyPrinter();
            or = new OrPrinter();
            xor = new XorPrinter();
            and = new AndPrinter();
            not = new NotPrinter();
            constants = new ConstantPrinter();
            vars = new VariablePrinter();
            paren = new ParenthesesPrinter();

            builder = new StringBuilder();
        }

        public void init() {
            init(Style.DEFAULT);
        }

        public void init(Style style) {
            init(iff, imply, style);
            init(imply, or, style);
            init(or, xor, style);
            init(xor, and, style);
            init(and, not, style);
            init(not, vars, style);
            init(vars, constants, style);
            init(constants, paren, style);
            init(paren, iff, style);
        }

        public void init(DelegatingPrinter printer, DelegatingPrinter next, Style style) {
            printer.setNext(next);
            printer.setBuilder(builder);
            printer.style = style;
        }

        public StringBuilder builder;
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

    public static class DelegatingPrinter implements BooleanVisitor<Void> {
        public DelegatingPrinter next;
        public StringBuilder builder;
        public Style style = Style.DEFAULT;

        public void setNext(DelegatingPrinter next) {
            this.next = next;
        }

        public void setBuilder(StringBuilder builder) {
            this.builder = builder;
        }

        public Void append(String text) {
            builder.append(text);
            return null;
        }

        protected Void visitBinary(DelegatingPrinter printer, String opSymbol, BinaryBooleanFormula node) {
            node.getX().accept(printer);
            append(opSymbol);
            node.getY().accept(printer);
            return null;
        }

        @Override
        public Void visit(And node) {
            return next.visit(node);
        }

        @Override
        public Void visit(Iff node) {
            return next.visit(node);
        }

        @Override
        public Void visit(Zero node) {
            return next.visit(node);
        }

        @Override
        public Void visit(One node) {
            return next.visit(node);
        }

        @Override
        public Void visit(Not node) {
            return next.visit(node);
        }

        @Override
        public Void visit(Imply node) {
            return next.visit(node);
        }

        @Override
        public Void visit(BooleanVariable node) {
            return next.visit(node);
        }

        @Override
        public Void visit(Or node) {
            return next.visit(node);
        }

        @Override
        public Void visit(Xor node) {
            return next.visit(node);
        }
    }

    public static class ImplyPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Imply node) {
            switch (style) {
            case UNICODE:
                return visitBinary(next, " \u21d2 ", node);
            default:
                return visitBinary(next, " => ", node);
            }
        }
    }

    public static class IffPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Iff node) {
            switch (style) {
            case REACH:
                return visitBinary(this, " <-> ", node);
            default:
                return visitBinary(this, " = ", node);
            }
        }
    }

    public static class OrPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Or node) {
            switch (style) {
            case VERILOG:
            case REACH:
                return visitBinary(this, " | ", node);
            default:
                return visitBinary(this, " + ", node);
            }
        }
    }

    public static class XorPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Xor node) {
            switch (style) {
            case UNICODE:
                return visitBinary(this, " \u2295 ", node);
            default:
                return visitBinary(this, " ^ ", node);
            }
        }
    }

    public static class AndPrinter extends DelegatingPrinter {
        @Override
        public Void visit(And node) {
            switch (style) {
            case UNICODE:
                return visitBinary(this, " \u00b7 ", node);
            case VERILOG:
            case REACH:
                return visitBinary(this, " & ", node);
            default:
                return visitBinary(this, " * ", node);
            }
        }
    }

    public static class NotPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Not node) {
            switch (style) {
            case UNICODE:
                append("\u00ac");
                return node.getX().accept(this);
            case VERILOG:
            case REACH:
                append("~");
                return node.getX().accept(this);
            default:
                node.getX().accept(this);
                return append("'");
            }
        }
    }

    public static class ConstantPrinter extends DelegatingPrinter {
        @Override
        public Void visit(One one) {
            switch (style) {
            case REACH:
                return append("true");
            default:
                return append("1");
            }
        }

        @Override
        public Void visit(Zero zero) {
            switch (style) {
            case REACH:
                return append("false");
            default:
                return append("0");
            }
        }
    }

    public static class VariablePrinter extends DelegatingPrinter {
        private final Map<String, BooleanVariable> varMap = new HashMap<>();

        @Override
        public Void visit(BooleanVariable var) {
            String label = var.getLabel();
            BooleanVariable nameHolder = varMap.get(label);
            if (nameHolder == null) {
                varMap.put(label, var);
            } else {
                if (nameHolder != var) {
                    throw new RuntimeException("Duplicate variable name '" + label + "'");
                }
            }

            switch (style) {
            case REACH:
                return append("$S\"" + label + "\"");

            default:
                return append(label);
            }
        }
    }

    public static class ParenthesesPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Zero node) {
            return enclose(node);
        }

        @Override
        public Void visit(One node) {
            return enclose(node);
        }

        @Override
        public Void visit(BooleanVariable node) {
            return enclose(node);
        }

        @Override
        public Void visit(And node) {
            return enclose(node);
        }

        @Override
        public Void visit(Or node) {
            return enclose(node);
        }

        @Override
        public Void visit(Xor node) {
            return enclose(node);
        }

        @Override
        public Void visit(Iff node) {
            return enclose(node);
        }

        @Override
        public Void visit(Imply node) {
            return enclose(node);
        }

        private Void enclose(BooleanFormula node) {
            append("(");
            node.accept(next);
            append(")");
            return null;
        }
    }

    public static String toString(BooleanFormula formula) {
        return toString(formula, Style.DEFAULT);
    }

    public static String toString(BooleanFormula formula, Style style) {
        if (formula == null) {
            return "";
        }
        DelegatingPrinter printer = getPrinter(style);
        formula.accept(printer);
        return printer.builder.toString();
    }

    private static DelegatingPrinter getPrinter(Style style) {
        PrinterSuite suite = new PrinterSuite();
        suite.init(style);
        return suite.iff;
    }

    @Override
    public String visit(And node) {
        return toString(node);
    }

    @Override
    public String visit(Iff node) {
        return toString(node);
    }

    @Override
    public String visit(Zero node) {
        return toString(node);
    }

    @Override
    public String visit(One node) {
        return toString(node);
    }

    @Override
    public String visit(Not node) {
        return toString(node);
    }

    @Override
    public String visit(Imply node) {
        return toString(node);
    }

    @Override
    public String visit(BooleanVariable node) {
        return toString(node);
    }

    @Override
    public String visit(Or node) {
        return toString(node);
    }

    @Override
    public String visit(Xor node) {
        return toString(node);
    }
}
