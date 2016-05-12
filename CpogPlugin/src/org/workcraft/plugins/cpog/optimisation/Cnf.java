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
package org.workcraft.plugins.cpog.optimisation;

import java.util.List;

import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class Cnf extends Nf<CnfClause> {

    public Cnf() {
    }

    public Cnf(CnfClause... clauses) {
        super(clauses);
    }

    public Cnf(List<CnfClause> clauses) {
        super(clauses);
    }

    @Override
    public String toString() {
        return new HumanReadableCnfPrinter().print(this);
    }

    public String toString(CnfPrinter cnfPrinter) {
        return cnfPrinter.print(this);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return BooleanOperations.and(getClauses()).accept(visitor);
    }
}
