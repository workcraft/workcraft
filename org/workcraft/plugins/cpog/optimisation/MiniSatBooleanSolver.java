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


public class MiniSatBooleanSolver {

	static final String minisatPath = "D:\\cheetah\\Programs\\cygwin\\bin\\MiniSat_v1.14_cygwin.exe";
	static final String claspPath = "C:\\Work\\Tools\\clasp-1.3.1\\clasp-1.3.1.exe";

	static final boolean useClasp = true;

	public BooleanSolution solve(CnfTask task)
	{
		String cnf = task.getBody();
		String solution = solve(cnf);

		return SolutionReader.readSolution(task, solution);
	}

	private String solve(String cnf) {
		if(!useClasp)
			return ProcessIO.minisat(cnf);
		else
			return ProcessIO.runViaStreams(cnf, new String[]{claspPath});
	}

}
