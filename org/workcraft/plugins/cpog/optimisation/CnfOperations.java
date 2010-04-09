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

import java.util.Arrays;
import java.util.List;

public class CnfOperations {
	public static CnfLiteral not(CnfLiteral x)
	{
		return new CnfLiteral(x.getVariable(), !x.getNegation());
	}

	public static CnfLiteral not(FreeVariable x)
	{
		return new CnfLiteral(x, true);
	}

	public static CnfClause or(List<FreeVariable> literals)
	{
		CnfClause result = new CnfClause();
		for(FreeVariable var : literals)
			result.getLiterals().add(literal(var));
		return result;
	}

	public static CnfClause or(CnfLiteral... literals)
	{
		return new CnfClause(Arrays.asList(literals));
	}

	public static CnfLiteral literal(FreeVariable var)
	{
		return new CnfLiteral(var);
	}
}
