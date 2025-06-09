package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

import java.util.HashMap;
import java.util.Map;

public class StringGenerator implements BooleanVisitor<String> {

    public enum Void {
    }

    public enum Style {
        DEFAULT, UNICODE, VERILOG, REACH, GENLIB, C
    }

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

        @SuppressWarnings("PMD.AvoidStringBufferField")
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
        @SuppressWarnings("PMD.AvoidStringBufferField")
        public StringBuilder builder;
        public DelegatingPrinter next;
        public Style style = Style.DEFAULT;

        public void setNext(DelegatingPrinter next) {
            this.next = next;
        }

        public void setBuilder(StringBuilder builder) {
            this.builder = builder;
        }

        public void append(String text) {
            if ((text != null) && !text.isEmpty()) {
                builder.append(text);
            }
        }

        void visitBinary(DelegatingPrinter printer, String opSymbol, BinaryBooleanFormula node) {
            node.getX().accept(printer);
            append(opSymbol);
            node.getY().accept(printer);
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
                case UNICODE -> visitBinary(next, " \u21d2 ", node);
                case VERILOG -> {
                    new Not(node.getX()).accept(this);
                    append(" | ");
                    node.getY().accept(this);
                }
                case C -> {
                    new Not(node.getX()).accept(this);
                    append(" || ");
                    node.getY().accept(this);
                }
                default -> visitBinary(next, " => ", node);
            }
            return null;
        }
    }

    public static class IffPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Iff node) {
            switch (style) {
                case REACH -> visitBinary(this, " <-> ", node);
                case VERILOG -> new Or(
                        new And(node.getX(), node.getY()),
                        new And(new Not(node.getX()), new Not(node.getY()))
                ).accept(this);
                case C -> {
                    // Negation of both operands is intentional to convert them into Booleans
                    new Not(node.getX()).accept(this);
                    append(" == ");
                    new Not(node.getY()).accept(this);
                }
                default -> visitBinary(this, " = ", node);
            }
            return null;
        }
    }

    public static class OrPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Or node) {
            switch (style) {
                case VERILOG, REACH -> visitBinary(this, " | ", node);
                case C -> visitBinary(this, " || ", node);
                default -> visitBinary(this, " + ", node);
            }
            return null;
        }
    }

    public static class XorPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Xor node) {
            switch (style) {
                case UNICODE -> visitBinary(this, " \u2295 ", node);
                case VERILOG -> new Not(new Or(
                        new And(node.getX(), node.getY()),
                        new And(new Not(node.getX()), new Not(node.getY()))
                )).accept(this);
                case C -> {
                    // Negation of both operands is intentional to convert them into Booleans
                    new Not(node.getX()).accept(this);
                    append(" != ");
                    new Not(node.getY()).accept(this);
                }
                default -> visitBinary(this, " ^ ", node);
            }
            return null;
        }
    }

    public static class AndPrinter extends DelegatingPrinter {
        @Override
        public Void visit(And node) {
            switch (style) {
                case UNICODE -> visitBinary(this, " \u00b7 ", node);
                case VERILOG, REACH -> visitBinary(this, " & ", node);
                case C -> visitBinary(this, " && ", node);
                default -> visitBinary(this, " * ", node);
            }
            return null;
        }
    }

    public static class NotPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Not node) {
            BooleanFormula x = node.getX();
            if (x instanceof Not) {
                // Simplify double inversion
                ((Not) x).getX().accept(this);
            } else {
                switch (style) {
                    case UNICODE -> {
                        append("\u00ac");
                        x.accept(this);
                    }
                    case VERILOG, REACH -> {
                        append("~");
                        x.accept(this);
                    }
                    case GENLIB, C -> {
                        append("!");
                        x.accept(this);
                    }
                    default -> {
                        x.accept(this);
                        append("'");
                    }
                }
            }
            return null;
        }
    }

    public static class ConstantPrinter extends DelegatingPrinter {
        @Override
        public Void visit(One one) {
            switch (style) {
                case REACH -> append("true");
                case GENLIB -> append("CONST1");
                default -> append("1");
            }
            return null;
        }

        @Override
        public Void visit(Zero zero) {
            switch (style) {
                case REACH -> append("false");
                case GENLIB -> append("CONST0");
                default -> append("0");
            }
            return null;
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
                case REACH -> append("$S\"" + label + "\"");
                default -> append(label);
            }
            return null;
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
