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
package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class FormulaToString implements BooleanVisitor<String> {
    public final class Void{
        private Void(){}
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
            init(false);
        }

        public void init(boolean unicodeAllowed) {
            init(iff, imply, unicodeAllowed);
            init(imply, or, unicodeAllowed);
            init(or, xor, unicodeAllowed);
            init(xor, and, unicodeAllowed);
            init(and, not, unicodeAllowed);
            init(not, vars, unicodeAllowed);
            init(vars, constants, unicodeAllowed);
            init(constants, paren, unicodeAllowed);
            init(paren, iff, unicodeAllowed);
        }

        public void init(DelegatingPrinter printer, DelegatingPrinter next, boolean unicodeAllowed) {
            printer.setNext(next);
            printer.setBuilder(builder);
            printer.unicodeAllowed = unicodeAllowed;
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

    public static class IffPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Iff node) {
            return visitBinary(this, " = ", node);
        }
    }

    public static class ImplyPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Imply node) {
            return visitBinary(next, unicodeAllowed ? " \u21d2 " : " => ", node);
        }
    }

    public static class OrPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Or node) {
            return visitBinary(this, " + ", node);
        }
    }

    public static class XorPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Xor node) {
            return visitBinary(this, unicodeAllowed ? " \u2295 " : " ^ ", node);
        }
    }

    public static class AndPrinter extends DelegatingPrinter {
        @Override
        public Void visit(And node) {
            return visitBinary(this, unicodeAllowed ? "\u00b7" : "*", node);
        }
    }

    public static class NotPrinter extends DelegatingPrinter {
        @Override
        public Void visit(Not node) {
            node.getX().accept(this);
            return append("'");
        }
    }

    public static class ConstantPrinter extends DelegatingPrinter {
        @Override
        public Void visit(One one) {
            return append("1");
        }
        @Override
        public Void visit(Zero zero) {
            return append("0");
        }
    }

    public static class VariablePrinter extends DelegatingPrinter {
        Map<String, BooleanVariable> varMap = new HashMap<String, BooleanVariable>();
        @Override
        public Void visit(BooleanVariable var) {
            String label = var.getLabel();
            BooleanVariable nameHolder = varMap.get(label);
            if(nameHolder == null)
                varMap.put(label, var);
            else
                if(nameHolder != var)
                    throw new RuntimeException("name conflict! duplicate name " + label);

            append(label);

            return null;
        }
    }

    public static class ParenthesesPrinter extends DelegatingPrinter {
        @Override public Void visit(Zero node) {
                return enclose(node);
        }
        @Override public Void visit(One node) {
                return enclose(node);
        }
        @Override public Void visit(BooleanVariable node) {
                return enclose(node);
        }
        @Override public Void visit(And node) {
                return enclose(node);
        }
        @Override public Void visit(Or node) {
                return enclose(node);
        }
        @Override public Void visit(Xor node) {
                return enclose(node);
        }
        @Override public Void visit(Iff node) {
                return enclose(node);
        }
        @Override public Void visit(Imply node) {
                return enclose(node);
        }
        Void enclose(BooleanFormula node) {
            append("(");
            node.accept(next);
            append(")");
            return null;
        }
    }

    public static String toString(BooleanFormula f) {
        return toString(f, false);
    }

    public static String toString(BooleanFormula f, boolean unicodeAllowed) {
        if (f==null) return "";
        DelegatingPrinter printer = getPrinter(unicodeAllowed);
        f.accept(printer);
        return printer.builder.toString();
    }

    private static DelegatingPrinter getPrinter(boolean unicodeAllowed) {
        PrinterSuite suite = new PrinterSuite();
        suite.init(unicodeAllowed);
        return suite.iff;
    }

    DelegatingPrinter printer;

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
