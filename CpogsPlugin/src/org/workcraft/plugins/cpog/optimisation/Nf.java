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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;


public abstract class Nf<C> implements BooleanFormula {

    private List<C> clauses = new ArrayList<C>();

    public Nf() {
    }

    public Nf(C... clauses) {
        this(Arrays.asList(clauses));
    }

    public Nf(List<C> clauses) {
        this.clauses = new ArrayList<C>(clauses);
    }

    public void setClauses(List<C> clauses) {
        this.clauses = new ArrayList<C>(clauses);
    }

    public List<C> getClauses() {
        return clauses;
    }

    public void add(List<C> list) {
        clauses.addAll(list);
    }

    public void add(C... arr) {
        clauses.addAll(Arrays.asList(arr));
    }

    public abstract <T> T accept(BooleanVisitor<T> visitor);

    public void add(Nf<C> nf) {
        add(nf.getClauses());
    }
}
