package org.workcraft.plugins.cpog.formula;

import java.util.HashMap;
import java.util.Map;

public class CpogFormulaToString implements CpogVisitor<String> {

    public enum Void {
    }

    public static class PrinterSuite {
        public StringBuilder builder;
        public OverlayPrinter overlay;
        public SequencePrinter sequence;
        public VariablePrinter vars;
        public ParenthesesPrinter paren;

        public PrinterSuite() {
            overlay = new OverlayPrinter();
            sequence = new SequencePrinter();
            vars = new VariablePrinter();
            paren = new ParenthesesPrinter();

            builder = new StringBuilder();
        }

        public void init() {
            init(false);
        }

        public void init(boolean unicodeAllowed) {
            init(overlay, sequence, unicodeAllowed);
            init(sequence, vars, unicodeAllowed);
            init(vars, paren, unicodeAllowed);
            init(paren, overlay, unicodeAllowed);
        }

        public void init(DelegatingPrinter printer, DelegatingPrinter next, boolean unicodeAllowed) {
            printer.setNext(next);
            printer.setBuilder(builder);
            printer.unicodeAllowed = unicodeAllowed;
        }
    }

    public static class DelegatingPrinter implements CpogVisitor<Void> {
        public DelegatingPrinter next;
        public StringBuilder builder;
        public boolean unicodeAllowed = false;

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

        protected Void visitBinary(DelegatingPrinter printer, String opSymbol, BinaryCpogFormula node) {
            node.getX().accept(printer);
            append(opSymbol);
            node.getY().accept(printer);
            return null;
        }

        @Override
        public Void visit(Overlay node) {
            return next.visit(node);
        }

        @Override
        public Void visit(Sequence node) {
            return next.visit(node);
        }

        @Override
        public Void visit(CpogFormulaVariable node) {
            return next.visit(node);
        }
    }

    public static class OverlayPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Overlay node) {
            return visitBinary(this, " + ", node);
        }
    }

    public static class SequencePrinter extends DelegatingPrinter {
        @Override
        public Void visit(Sequence node) {
            return visitBinary(this, unicodeAllowed ? " \u2295 " : " ^ ", node);
        }
    }

    public static class VariablePrinter extends DelegatingPrinter {
        private final Map<String, CpogFormulaVariable> varMap = new HashMap<>();

        @Override
        public Void visit(CpogFormulaVariable var) {
            String label = var.getLabel();
            CpogFormulaVariable nameHolder = varMap.get(label);
            if (nameHolder == null) {
                varMap.put(label, var);
            } else {
                if (nameHolder != var) {
                    throw new RuntimeException("name conflict! duplicate name " + label);
                }
            }
            append(label);
            return null;
        }
    }

    public static class ParenthesesPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Overlay node) {
            return enclose(node);
        }

        @Override
        public Void visit(Sequence node) {
            return enclose(node);
        }

        private Void enclose(CpogFormula node) {
            append("(");
            node.accept(next);
            append(")");
            return null;
        }
    }

    public static String toString(CpogFormula f) {
        return toString(f, false);
    }

    public static String toString(CpogFormula f, boolean unicodeAllowed) {
        if (f == null) return "";
        DelegatingPrinter printer = getPrinter(unicodeAllowed);
        f.accept(printer);
        return printer.builder.toString();
    }

    private static DelegatingPrinter getPrinter(boolean unicodeAllowed) {
        PrinterSuite suite = new PrinterSuite();
        suite.init(unicodeAllowed);
        return suite.overlay;
    }

    @Override
    public String visit(Overlay node) {
        return toString(node);
    }

    @Override
    public String visit(Sequence node) {
        return toString(node);
    }

    @Override
    public String visit(CpogFormulaVariable node) {
        return toString(node);
    }

}
