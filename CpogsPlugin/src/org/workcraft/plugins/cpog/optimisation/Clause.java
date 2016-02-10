/*
*
* Copyright 2008,2009,2010 Newcastle University
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;


public abstract class Clause implements BooleanFormula {

    private List<Literal> literals = new ArrayList<Literal>();

    public Clause() {
    }

    public Clause(Literal... literals) {
        this(Arrays.asList(literals));
    }

    public Clause(List<Literal> literals) {
        this.setLiterals(literals);
    }

    public void setLiterals(List<Literal> literals) {
        this.literals = literals;
    }

    public List<Literal> getLiterals() {
        return literals;
    }

    public void add(List<Literal> list) {
        literals.addAll(list);
    }

    public void add(Literal... arr) {
        literals.addAll(Arrays.asList(arr));
    }

    public abstract <T> T accept(BooleanVisitor<T> visitor);

}
