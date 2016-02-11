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

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.and;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.iff;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.imply;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.not;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.or;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.xor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class BooleanReplacer implements BooleanVisitor<BooleanFormula> {

    interface BinaryOperation {
        BooleanFormula apply(BooleanFormula x, BooleanFormula y);
    }

    private final HashMap<BooleanFormula, BooleanFormula> map;
    private final BooleanWorker worker;

    public BooleanReplacer(List<? extends BooleanVariable> from, List<? extends BooleanFormula> to, BooleanWorker worker) {
        this.map = new HashMap<BooleanFormula, BooleanFormula>();
        if (from.size() != to.size()) {
            throw new RuntimeException("Length of the variable list must be equal to that of formula list.");
        }
        for(int i = 0; i < from.size(); i++) {
            this.map.put(from.get(i), to.get(i));
        }
        this.worker = worker;
    }

    public BooleanReplacer(Map<? extends BooleanVariable, ? extends BooleanFormula> map, BooleanWorker worker) {
        this.map = new HashMap<BooleanFormula, BooleanFormula>(map);
        this.worker = worker;
    }

    protected BooleanFormula visitBinaryFunc(BinaryBooleanFormula node, BinaryOperation op) {
        BooleanFormula result = map.get(node);
        if(result == null) {
            BooleanFormula x = node.getX().accept(this);
            BooleanFormula y = node.getY().accept(this);
            if(node.getX() == x && node.getY() == y) {
                result = node;
            } else {
                result = op.apply(x, y);
            }
            map.put(node, result);
        }
        return result;
    }

    @Override
    public BooleanFormula visit(Zero node) {
        return node;
    }

    @Override
    public BooleanFormula visit(One node) {
        return node;
    }

    @Override
    public BooleanFormula visit(BooleanVariable node) {
        BooleanFormula replacement = map.get(node);
        return replacement != null ? replacement : node;
    }

    @Override
    public BooleanFormula visit(Not node) {
        BooleanFormula result = map.get(node);
        if (result == null) {
            BooleanFormula x = node.getX().accept(this);
            if(node.getX() == x) {
                result = node;
            } else {
                result = not(x);
            }
            map.put(node, result);
        }
        return result;
    }

    @Override
    public BooleanFormula visit(And node) {
        return visitBinaryFunc(node, new BinaryOperation() {
            @Override
            public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
                return and(x, y, worker);
            }
        });
    }

    @Override
    public BooleanFormula visit(Or node) {
        return visitBinaryFunc(node, new BinaryOperation() {
            @Override
            public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
                return or(x, y, worker);
            }
        });
    }

    @Override
    public BooleanFormula visit(Iff node) {
        return visitBinaryFunc(node, new BinaryOperation() {
            @Override
            public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
                return iff(x, y, worker);
            }
        });
    }

    @Override
    public BooleanFormula visit(Xor node) {
        return visitBinaryFunc(node, new BinaryOperation() {
            @Override
            public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
                return xor(x, y, worker);
            }
        });
    }

    @Override
    public BooleanFormula visit(Imply node) {
        return visitBinaryFunc(node, new BinaryOperation() {
            @Override
            public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
                return imply(x, y, worker);
            }
        });
    }

}
