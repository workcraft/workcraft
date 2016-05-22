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
package org.workcraft.formula.encoding;

import java.util.Arrays;
import java.util.List;

import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Literal;
import org.workcraft.formula.cnf.CnfClause;

public class CnfOperations {
    public static Literal not(Literal x) {
        return new Literal(x.getVariable(), !x.getNegation());
    }

    public static Literal not(BooleanVariable x) {
        return new Literal(x, true);
    }

    public static CnfClause or(List<BooleanVariable> literals) {
        CnfClause result = new CnfClause();
        for (BooleanVariable var : literals) {
            result.getLiterals().add(literal(var));
        }
        return result;
    }

    public static CnfClause or(Literal... literals) {
        return new CnfClause(Arrays.asList(literals));
    }

    public static Literal literal(BooleanVariable var) {
        return new Literal(var);
    }
}
