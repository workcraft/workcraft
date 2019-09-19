package org.workcraft.plugins.circuit.naryformula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class NaryBooleanFormulaBuilder {

    private static class NaryBooleanFormulaPrinter implements BooleanVisitor<NaryBooleanFormula> {

        public List<NaryBooleanFormula> visitBinaryOp(BinaryBooleanFormula op, NaryBooleanFormulaVisitor<List<NaryBooleanFormula>> extractor) {
            NaryBooleanFormula x = op.getX().accept(this);
            NaryBooleanFormula y = op.getY().accept(this);
            return extractAndMerge(x, y, extractor);
        }

        @Override
        public NaryBooleanFormula visit(And node) {
            return NaryUtils.getAnd(visitBinaryOp(node, new AndArgExtractor()));
        }

        @Override
        public NaryBooleanFormula visit(Or node) {
            return NaryUtils.getOr(visitBinaryOp(node, new OrArgExtractor()));
        }

        @Override
        public NaryBooleanFormula visit(Xor node) {
            return NaryUtils.getXor(visitBinaryOp(node, new XorArgExtractor()));
        }

        private ArrayList<NaryBooleanFormula> extractAndMerge(
                NaryBooleanFormula x,
                NaryBooleanFormula y,
                NaryBooleanFormulaVisitor<List<NaryBooleanFormula>> extractor) {
            ArrayList<NaryBooleanFormula> result = new ArrayList<>();
            result.addAll(x.accept(extractor));
            result.addAll(y.accept(extractor));
            return result;
        }

        @Override
        public NaryBooleanFormula visit(Iff node) {
            return visit(new Not(new Xor(node.getX(), node.getY())));
        }

        @Override
        public NaryBooleanFormula visit(Zero node) {
            return NaryUtils.getOr(Collections.<NaryBooleanFormula>emptyList());
        }

        @Override
        public NaryBooleanFormula visit(One node) {
            return NaryUtils.getAnd(Collections.<NaryBooleanFormula>emptyList());
        }

        @Override
        public NaryBooleanFormula visit(Not node) {
            return NaryUtils.getNot(node.getX().accept(this));
        }

        @Override
        public NaryBooleanFormula visit(Imply node) {
            return visit(new Or(new Not(node.getX()), node.getY()));
        }

        @Override
        public NaryBooleanFormula visit(BooleanVariable variable) {
            return NaryUtils.getVar(variable);
        }
    }

    private static final class AndArgExtractor extends NaryDefaultArgExtractor {
        @Override
        public List<NaryBooleanFormula> visitAnd(List<NaryBooleanFormula> args) {
            return args;
        }
    }

    private static final class OrArgExtractor extends NaryDefaultArgExtractor {
        @Override
        public List<NaryBooleanFormula> visitOr(List<NaryBooleanFormula> args) {
            return args;
        }
    }

    private static final class XorArgExtractor extends NaryDefaultArgExtractor {
        @Override
        public List<NaryBooleanFormula> visitXor(List<NaryBooleanFormula> args) {
            return args;
        }
    }

    public static NaryBooleanFormula build(BooleanFormula formula) {
        return formula.accept(new NaryBooleanFormulaPrinter());
    }

}
