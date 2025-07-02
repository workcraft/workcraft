package org.workcraft.plugins.cpog.sat;

import org.workcraft.formula.BooleanVariable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class SolutionReader {

    private static Map<Integer, String> extractCnfMapping(String cnf) {
        HashMap<Integer, String> map = new HashMap<>();

        BufferedReader reader = new BufferedReader(new StringReader(cnf));
        while (true) {
            String line;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (line == null) {
                break;
            }

            if (line.charAt(0) == 'c') {
                String[] split = line.split(" ");
                map.put(Integer.parseInt(split[1]), split[2]);
            }
        }

        return map;
    }

    private static final class BooleanSolutionImplementation implements BooleanSolution {
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
            if (result == null) {
                throw new RuntimeException("No solution for variable: " + variable.getLabel());
            }
            return result;
        }
    }

    public static BooleanSolution readSolution(CnfTask task, String solution) {
        List<Integer> numbers = extractNumbers(solution);
        if (numbers == null) {
            return null;
        }

        final Map<BooleanVariable, Boolean> results = new HashMap<>();
        Map<Integer, String> cnfToOriginal = extractCnfMapping(task.getBody());
        Map<String, BooleanVariable> vars = task.getVars();
        for (int number : numbers) {
            String varName = cnfToOriginal.get(Math.abs(number));
            if (varName != null) {
                boolean value = (number >= 0);
                if ("0".equals(varName)) {
                    if (value) {
                        throw new RuntimeException("0");
                    }
                } else if ("1".equals(varName)) {
                    if (!value) {
                        throw new RuntimeException("!1");
                    }
                } else {
                    BooleanVariable var = vars.get(varName);
                    if (var == null) {
                        throw new RuntimeException("No variable for solution:" + varName);
                    }
                    results.put(var, value);
                }
            }
        }

        return new BooleanSolutionImplementation(results);
    }

    private static List<Integer> extractNumbers(String solution) {
        String[] split = solution.split("\n");

        if ("UNSAT".equals(split[0])) {
            return null;
        }
        if ("SAT".equals(split[0])) { //MPSAT file
            if (split.length != 2) {
                throw new RuntimeException("Minisat output is more than 2 lines.");
            }
            return parseIntArray(split[1].split(" "));
        } else { //clasp file
            boolean sat = false;
            for (String s : split) {
                if ("s UNSATISFIABLE".equals(s)) {
                    return null;
                }
                if ("s SATISFIABLE".equals(s)) {
                    sat = true;
                    break;
                }
            }

            if (!sat) {
                throw new RuntimeException("no information on satisfiability found");
            }

            List<Integer> result = new ArrayList<>();
            for (int i = 1; i < split.length; i++) {
                if (!split[i].isEmpty() && (split[i].charAt(0) == 'v')) {
                    String[] nums = split[i].split(" ");

                    for (int j = 1; j < nums.length; j++) {
                        result.add(Integer.parseInt(nums[j]));
                    }
                }
            }
            return result;
        }
    }

    private static List<Integer> parseIntArray(String[] nums) {
        List<Integer> result = new ArrayList<>();
        for (String num : nums) {
            result.add(Integer.parseInt(num));
        }

        return result;
    }

}
