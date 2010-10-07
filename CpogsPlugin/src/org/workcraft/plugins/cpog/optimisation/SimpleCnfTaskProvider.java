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

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.not;
import static org.workcraft.plugins.cpog.optimisation.CnfOperations.or;

import java.util.HashMap;
import java.util.Map;

public class SimpleCnfTaskProvider implements RawCnfGenerator<Cnf>
{
	@Override
	public CnfTask getCnf(Cnf cnf) {
		Map<String, BooleanVariable> vars = new HashMap<String, BooleanVariable>();

		for(CnfClause clause : cnf.getClauses())
			for(Literal literal : clause.getLiterals()) {
				BooleanVariable variable = literal.getVariable();
				String label = variable.getLabel();
				if(!label.isEmpty())
					vars.put(label, variable);
			}

		cnf.getClauses().add(or(not(Literal.Zero)));
		cnf.getClauses().add(or(Literal.One));

		return new CnfTask(cnf.toString(new MiniSatCnfPrinter()), vars);
	}
}
