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
package org.workcraft.formula;

public class DumbBooleanWorker implements BooleanWorker {
    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        return new And(x, y);
    }

    @Override
    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        return new Iff(x, y);
    }

    @Override
    public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        return new Imply(x, y);
    }

    @Override
    public BooleanFormula not(BooleanFormula x) {
        return new Not(x);
    }

    @Override
    public BooleanFormula one() {
        return One.instance();
    }

    @Override
    public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
        return new Or(x, y);
    }

    @Override
    public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        return not(new Iff(x, y));
    }

    @Override
    public BooleanFormula zero() {
        return Zero.instance();
    }

}
