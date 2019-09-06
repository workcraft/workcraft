package org.workcraft.formula.sat;

import org.workcraft.formula.BooleanSolution;
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
        Map<String, BooleanVariable> vars = task.getVars();

        Map<Integer, String> cnfToOriginal = extractCnfMapping(task.getBody());

        List<Integer> numbers = extractNumbers(solution);
        if (numbers == null) {
            return null;
        }

        final Map<BooleanVariable, Boolean> results = new HashMap<>();
        for (int i = 0; i < numbers.size(); i++) {
            int cnfIndex = numbers.get(i);
            boolean value = cnfIndex >= 0;
            if (!value) {
                cnfIndex = -cnfIndex;
            }
            String varName = cnfToOriginal.get(cnfIndex);
            if (varName != null) {
                if (varName.equals("0")) {
                    if (value) {
                        throw new RuntimeException("0");
                    }
                } else if (varName.equals("1")) {
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

        if (split[0].equals("UNSAT")) {
            return null;
        }
        if (split[0].equals("SAT")) { //MPSAT file
            if (split.length != 2) {
                throw new RuntimeException("Minisat output is more than 2 lines.");
            }
            return parseIntArray(split[1].split(" "));
        } else { //clasp file
            boolean sat = false;
            for (int i = 0; i < split.length; i++) {
                if (split[i].equals("s UNSATISFIABLE")) {
                    return null;
                }
                if (split[i].equals("s SATISFIABLE")) {
                    sat = true;
                    break;
                }
            }

            if (!sat) {
                throw new RuntimeException("no information on satisfiability found");
            }

            List<Integer> result = new ArrayList<>();
            for (int i = 1; i < split.length; i++) {
                if (split[i].length() > 0 && split[i].charAt(0) == 'v') {
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

        for (int i = 0; i < nums.length; i++) {
            result.add(Integer.parseInt(nums[i]));
        }

        return result;
    }

}
