package org.workcraft.formula.sat;

import org.workcraft.formula.*;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.cnf.CnfClause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.workcraft.formula.encoding.CnfOperations.*;

public class CleverCnfGenerator implements CnfGenerator<BooleanFormula>, BooleanVisitor<Literal> {

    Cnf result = new Cnf();

    private static final class Void {
        private Void() { }
    }

    static class ConstantExpectingCnfGenerator implements BooleanVisitor<Void> {
        private static boolean cleverOptimiseAnd = true;

        private final BooleanVisitor<Literal> dumbGenerator;
        private final Cnf result;

        ConstantExpectingCnfGenerator(Cnf result, BooleanVisitor<Literal> dumbGenerator) {
            this.result = result;
            this.dumbGenerator = dumbGenerator;
        }

        boolean currentResult = true;

        @Override
        public Void visit(And and) {
            if (currentResult) {
                and.getX().accept(this);
                and.getY().accept(this);
            } else {
                if (!cleverOptimiseAnd) {
                    Literal x = and.getX().accept(dumbGenerator);
                    Literal y = and.getY().accept(dumbGenerator);
                    result.addClauses(or(not(x), not(y)));
                } else {
                    Literal[][] side1 = getBiClause(and.getX());
                    Literal[][] side2 = getBiClause(and.getY());
                    for (int i = 0; i < side1.length; i++) {
                        for (int j = 0; j < side2.length; j++) {
                            List<Literal> list = new ArrayList<>();
                            for (int k = 0; k < side1[i].length; k++) {
                                list.add(not(side1[i][k]));
                            }
                            for (int k = 0; k < side2[j].length; k++) {
                                list.add(not(side2[j][k]));
                            }
                            result.addClauses(new CnfClause(list));
                        }
                    }
                }

            }
            return null;
        }

        static class BiClauseGenerator implements BooleanVisitor<Literal[][]> {
            BooleanVisitor<Literal> dumbGenerator;
            BiClauseGenerator(BooleanVisitor<Literal> dumbGenerator) {
                this.dumbGenerator = dumbGenerator;
            }
            @Override
            public Literal[][] visit(And node) {
                Literal[][] result = new Literal[1][];
                result[0] = new Literal[2];
                result[0][0] = node.getX().accept(dumbGenerator);
                result[0][1] = node.getY().accept(dumbGenerator);
                return result;
            }
            @Override
            public Literal[][] visit(Iff node) {
                throw new RuntimeException();
            }
            @Override
            public Literal[][] visit(Zero node) {
                throw new RuntimeException();
            }
            @Override
            public Literal[][] visit(One node) {
                throw new RuntimeException();
            }
            @Override
            public Literal[][] visit(Not node) {
                Literal[][] preres = node.getX().accept(this);
                if (preres.length != 1) {
                    throw new RuntimeException("something wrong...");
                }
                Literal[][]res = new Literal[preres[0].length][];
                for (int i = 0; i < res.length; i++) {
                    res[i] = new Literal[1];
                    res[i][0] = not(preres[0][i]);
                }
                return res;
            }
            @Override
            public Literal[][] visit(Imply node) {
                throw new RuntimeException();
            }
            @Override
            public Literal[][] visit(BooleanVariable variable) {
                Literal[][]result = new Literal[1][];
                result[0] = new Literal[1];
                result[0][0] = literal(variable);
                return result;
            }
            @Override
            public Literal[][] visit(Or node) {
                throw new RuntimeException();
            }
            @Override
            public Literal[][] visit(Xor node) {
                throw new RuntimeException();
            }
        }

        Literal[][] getBiClause(BooleanFormula formula) {
            return formula.accept(new BiClauseGenerator(dumbGenerator));
        }

        @Override
        public Void visit(Iff iff) {
            Literal x = iff.getX().accept(dumbGenerator);
            Literal y = iff.getY().accept(dumbGenerator);
            if (currentResult) {
                result.addClauses(or(x, not(y)));
                result.addClauses(or(not(x), y));
            } else {
                result.addClauses(or(x, y));
                result.addClauses(or(not(x), not(y)));
            }
            return null;
        }

        @Override
        public Void visit(Not not) {
            boolean store = currentResult;
            currentResult = !currentResult;
            not.getX().accept(this);
            currentResult = store;
            return null;
        }

        @Override
        public Void visit(BooleanVariable node) {
            if (currentResult) {
                result.addClauses(or(literal(node)));
            } else {
                result.addClauses(or(not(node)));
            }
            return null;
        }
        @Override
        public Void visit(Zero node) {
            throw new RuntimeException("not implemented");
        }
        @Override
        public Void visit(One node) {
            throw new RuntimeException("not implemented");
        }
        @Override
        public Void visit(Imply node) {
            throw new RuntimeException("not implemented");
        }
        @Override
        public Void visit(Or node) {
            throw new RuntimeException("not implemented");
        }

        @Override
        public Void visit(Xor node) {
            throw new RuntimeException("not implemented");
        }
    }

    public CnfTask getCnf(BooleanFormula formula) {
        Cnf cnf = generate(formula);
        return new SimpleCnfTaskProvider().getCnf(cnf);
    }

    public Cnf generate(BooleanFormula formula) {
        formula.accept(new ConstantExpectingCnfGenerator(result, this));
        return result;
    }

    Map<BooleanFormula, Literal> cache = new HashMap<>();

    Literal newVar(BooleanFormula node) {
        Literal res = new Literal("");
        cache.put(node, res);
        return res;
    }

    interface BinaryGateImplementer {
        void implement(Literal res, Literal x, Literal y);
    }

    public Literal visit(BinaryBooleanFormula node, BinaryGateImplementer impl) {
        Literal res = cache.get(node);
        if (res == null) {
            res = newVar(node);
            Literal x = node.getX().accept(this);
            Literal y = node.getY().accept(this);
            impl.implement(res, x, y);
        }
        return res;
    }

    @Override
    public Literal visit(And node) {
        return visit(node,
            (res, x, y) -> {
                result.addClauses(or(res, not(x), not(y)));
                result.addClauses(or(not(res), x));
                result.addClauses(or(not(res), y));
            }
        );
    }

    @Override
    public Literal visit(Iff node) {
        return visit(node,
                (res, x, y) -> {
                    result.addClauses(or(not(res), not(x), y));
                    result.addClauses(or(not(res), x, not(y)));
                    result.addClauses(or(res, not(x), not(y)));
                    result.addClauses(or(res, x, y));
                }
            );
    }

    @Override
    public Literal visit(Zero node) {
        return Literal.ZERO;
    }

    @Override
    public Literal visit(One node) {
        return Literal.ONE;
    }

    @Override
    public Literal visit(Not node) {
        return not(node.getX().accept(this));
    }

    @Override
    public Literal visit(Imply node) {
        return visit(node,
                (res, x, y) -> {
                    result.addClauses(or(not(res), not(x), y));
                    result.addClauses(or(res, not(y)));
                    result.addClauses(or(res, x));
                }
            );
    }

    @Override
    public Literal visit(BooleanVariable variable) {
        return literal(variable);
    }

    @Override
    public Literal visit(Or node) {
        return visit(node,
                (res, x, y) -> {
                    result.addClauses(or(not(res), x, y));
                    result.addClauses(or(res, not(y)));
                    result.addClauses(or(res, not(x)));
                }
            );
    }

    @Override
    public Literal visit(Xor node) {
        return visit(node,
                (res, x, y) -> {
                    result.addClauses(or(res, not(x), y));
                    result.addClauses(or(res, x, not(y)));
                    result.addClauses(or(not(res), not(x), not(y)));
                    result.addClauses(or(not(res), x, y));
                }
            );
    }

}
