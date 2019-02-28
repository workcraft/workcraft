package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.gui.tools.Trace;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MpsatOutputParser {

    private final LinkedList<MpsatSolution> solutions;

    /*
     * \R -- any Unicode linebreak sequence introduced in Java 8.
     * It is equivalent to \u000D\u000A\u000A\u000B\u000C\u000D\u0085\u2028\u2029].
     */

    private static final Pattern patternReachability0 = Pattern.compile(
            "SOLUTION .+\\R" +
            "total cost of all paths: .+\\R",
            Pattern.UNIX_LINES);

    private static final Pattern patternReachability1 = Pattern.compile(
            "SOLUTION .+\\R" +
            "(.*)\\R" +
            "path cost: .+\\R",
            Pattern.UNIX_LINES);

    private static final Pattern patternReachability2 = Pattern.compile(
            "SOLUTION .+\\R" +
            "(.*)\\R" +
            "(.*)\\R" +
            "total cost of all paths: .+\\R" +
            "(\\RConflict for signal (.+)\\R)?",
            Pattern.UNIX_LINES);

    private static final Pattern patternNormalcy1 = Pattern.compile(
            "SOLUTION .+\\R" +
            "(.*)\\R" +
            "triggers: .+\\R",
            Pattern.UNIX_LINES);

    public MpsatOutputParser(MpsatOutput mpsatOutput) {
        String mpsatStdout = mpsatOutput.getStdoutString();
        solutions = new LinkedList<>();
        Matcher matcherReachability0 = patternReachability0.matcher(mpsatStdout);
        while (matcherReachability0.find()) {
            MpsatSolution solution = new MpsatSolution(null, null);
            solutions.add(solution);
        }
        Matcher matcherReachability1 = patternReachability1.matcher(mpsatStdout);
        while (matcherReachability1.find()) {
            Trace trace = getTrace(matcherReachability1.group(1));
            MpsatSolution solution = new MpsatSolution(trace, null);
            solutions.add(solution);
        }
        Matcher matcherRreachability2 = patternReachability2.matcher(mpsatStdout);
        while (matcherRreachability2.find()) {
            Trace mainTrace = getTrace(matcherRreachability2.group(1));
            Trace branchTrace = getTrace(matcherRreachability2.group(2));
            String signalName = matcherRreachability2.group(4);
            MpsatSolution solution = new MpsatSolution(mainTrace, branchTrace);
            solution.setComment(signalName);
            solutions.add(solution);
        }
        Matcher matcherNormalcy = patternNormalcy1.matcher(mpsatStdout);
        while (matcherNormalcy.find()) {
            Trace trace = getTrace(matcherNormalcy.group(1));
            MpsatSolution solution = new MpsatSolution(trace, null);
            solutions.add(solution);
        }
    }

    private Trace getTrace(String mpsatTrace) {
        Trace trace = null;
        if (mpsatTrace != null) {
            trace = new Trace();
            String[] mpsatTransitions = mpsatTrace.replaceAll("\\s", "").split(",");
            for (String mpsatTransition: mpsatTransitions) {
                String transition = mpsatTransition.substring(mpsatTransition.indexOf(':') + 1);
                if (!transition.isEmpty()) {
                    trace.add(transition);
                }
            }
        }
        return trace;
    }

    public List<MpsatSolution> getSolutions() {
        return Collections.unmodifiableList(solutions);
    }

}
