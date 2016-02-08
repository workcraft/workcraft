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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolutionReader
{
    private static Map<Integer, String> extractCnfMapping(String cnf) {
        HashMap<Integer, String> map = new HashMap<Integer, String>();

        BufferedReader reader = new BufferedReader(new StringReader(cnf));
        while(true)
        {
            String line;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(line==null)
                break;

            if(line.charAt(0)=='c')
            {
                String[] split = line.split(" ");
                map.put(Integer.parseInt(split[1]), split[2]);
            }
        }

        return map;
    }

    private static final class BooleanSolutionImplementation implements BooleanSolution
    {
        private final Map<BooleanVariable, Boolean> results;

        private BooleanSolutionImplementation(Map<BooleanVariable, Boolean> results) {
            this.results = results;
        }

        @Override
        public Collection<BooleanVariable> getVariables() {
            return results.keySet();
        }

        @Override
        public boolean getSolution(BooleanVariable variable) {
            Boolean result = results.get(variable);
            if(result == null)
                throw new RuntimeException("No solution for variable: " + variable.getLabel());
            return result;
        }
    }

    public static BooleanSolution readSolution(CnfTask task, String solution) {
        Map<String, BooleanVariable> vars = task.getVars();

        Map<Integer, String> cnfToOriginal = extractCnfMapping(task.getBody());


        List<Integer> numbers = extractNumbers(solution);
        if(numbers==null)
            return null;

        final Map<BooleanVariable, Boolean> results = new HashMap<BooleanVariable, Boolean>();
        for(int i = 0; i < numbers.size(); i++)
        {
            int cnfIndex = numbers.get(i);
            boolean value = cnfIndex>=0;
            if(!value)
                cnfIndex=-cnfIndex;
            String varName = cnfToOriginal.get(cnfIndex);
            if(varName != null)
            {
                if(varName.equals("0"))
                {
                    if(value)
                        throw new RuntimeException("0");
                }
                else if(varName.equals("1"))
                {
                    if(!value)
                        throw new RuntimeException("!1");
                }
                else
                {
                    BooleanVariable var = vars.get(varName);
                    if(var == null)
                        throw new RuntimeException("No variable for solution:" + varName);
                    results.put(var, value);
                }
            }
        }

        return new BooleanSolutionImplementation(results);
    }

    private static List<Integer> extractNumbers(String solution) {
        String[] split = solution.split("\n");

        if(split[0].equals("UNSAT"))
            return null;
        if(split[0].equals("SAT"))
        {//MPSAT file
            if(split.length != 2)
                throw new RuntimeException("Minisat output is more than 2 lines.");
            return parseIntArray(split[1].split(" "));
        }
        else
        {//clasp file
            boolean sat = false;
            for(int i=0;i<split.length;i++)
            {
                if(split[i].equals("s UNSATISFIABLE"))
                    return null;
                if(split[i].equals("s SATISFIABLE"))
                {
                    sat = true;
                    break;
                }
            }

            if(!sat)
                throw new RuntimeException("no information on satisfiability found");

            List<Integer> result = new ArrayList<Integer>();
            for(int i=1;i<split.length;i++)
            {
                if(split[i].length()>0 && split[i].charAt(0) == 'v')
                {
                    String[] nums = split[i].split(" ");

                    for(int j=1;j<nums.length;j++)
                        result.add(Integer.parseInt(nums[j]));
                }
            }
            return result;
        }
    }

    private static List<Integer> parseIntArray(String[] nums) {
        List<Integer> result = new ArrayList<Integer>();

        for(int i=0;i<nums.length;i++)
            result.add(Integer.parseInt(nums[i]));

        return result;
    }

}
