package org.workcraft.plugins.circuit.naryformula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class NaryFormulaBuilder {

    private static final class AndArgExtractor extends DefaultArgExtractor {
        @Override
        public List<NaryBooleanFormula> visitAnd(List<NaryBooleanFormula> args) {
            return args;
        }
    }

    private static final class OrArgExtractor extends DefaultArgExtractor {
        @Override
        public List<NaryBooleanFormula> visitOr(List<NaryBooleanFormula> args) {
            return args;
        }
    }

    private static final class XorArgExtractor extends DefaultArgExtractor {
        @Override
        public List<NaryBooleanFormula> visitXor(List<NaryBooleanFormula> args) {
            return args;
        }
    }

    public static NaryBooleanFormula make(BooleanFormula f) {
        return f.accept(new BooleanVisitor<NaryBooleanFormula>() {

            List<NaryBooleanFormula> visitBinaryOp(BinaryBooleanFormula op, NaryBooleanFormulaVisitor<List<NaryBooleanFormula>> extractor){
                NaryBooleanFormula x = op.getX().accept(this);
                NaryBooleanFormula y = op.getY().accept(this);
                return extractAndMerge(x, y, extractor);
            }

            @Override
            public NaryBooleanFormula visit(And node) {
                return Util.getAnd(visitBinaryOp(node, new AndArgExtractor()));
            }

            @Override
            public NaryBooleanFormula visit(Or node) {
                return Util.getOr(visitBinaryOp(node, new OrArgExtractor()));
            }

            @Override
            public NaryBooleanFormula visit(Xor node) {
                return Util.getXor(visitBinaryOp(node, new XorArgExtractor()));
            }
            private ArrayList<NaryBooleanFormula> extractAndMerge(
                    NaryBooleanFormula x,
                    NaryBooleanFormula y,
                    NaryBooleanFormulaVisitor<List<NaryBooleanFormula>> extractor) {
                ArrayList<NaryBooleanFormula> result = new ArrayList<NaryBooleanFormula>();
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
                return Util.getOr(Collections.<NaryBooleanFormula>emptyList());
            }

            @Override
            public NaryBooleanFormula visit(One node) {
                return Util.getAnd(Collections.<NaryBooleanFormula>emptyList());
            }

            @Override
            public NaryBooleanFormula visit(Not node) {
                return Util.getNot(node.getX().accept(this));
            }

            @Override
            public NaryBooleanFormula visit(Imply node) {
                return visit(new Or(new Not(node.getX()), node.getY()));
            }

            @Override
            public NaryBooleanFormula visit(BooleanVariable variable) {
                return Util.getVar(variable);
            }
        });
    }
}
