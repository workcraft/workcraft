package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.utils.TraceUtils;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerificationOutputParser {

    private final LinkedList<Solution> solutions;

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

    public VerificationOutputParser(VerificationOutput mpsatOutput) {
        String mpsatStdout = mpsatOutput.getStdoutString();
        solutions = new LinkedList<>();
        Matcher matcherReachability0 = patternReachability0.matcher(mpsatStdout);
        while (matcherReachability0.find()) {
            Solution solution = new Solution();
            solutions.add(solution);
        }
        Matcher matcherReachability1 = patternReachability1.matcher(mpsatStdout);
        while (matcherReachability1.find()) {
            Solution solution = new Solution(TraceUtils.deserialiseTrace(matcherReachability1.group(1)));
            solutions.add(solution);
        }
        Matcher matcherRreachability2 = patternReachability2.matcher(mpsatStdout);
        while (matcherRreachability2.find()) {
            Trace mainTrace = TraceUtils.deserialiseTrace(matcherRreachability2.group(1));
            Trace branchTrace = TraceUtils.deserialiseTrace(matcherRreachability2.group(2));
            String signalName = matcherRreachability2.group(4);
            Solution solution = new Solution(mainTrace, branchTrace);
            solution.setComment(signalName);
            solutions.add(solution);
        }
        Matcher matcherNormalcy = patternNormalcy1.matcher(mpsatStdout);
        while (matcherNormalcy.find()) {
            Trace trace = TraceUtils.deserialiseTrace(matcherNormalcy.group(1));
            Solution solution = new Solution(trace);
            solutions.add(solution);
        }
    }

    public List<Solution> getSolutions() {
        return Collections.unmodifiableList(solutions);
    }

}
