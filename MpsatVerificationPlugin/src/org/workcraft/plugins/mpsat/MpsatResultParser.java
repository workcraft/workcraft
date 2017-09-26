package org.workcraft.plugins.mpsat;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.Trace;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;

public class MpsatResultParser {
    private final LinkedList<MpsatSolution> solutions;

    private static final Pattern patternReachability0 =
            Pattern.compile("SOLUTION .+\ntotal cost of all paths: .+\n", Pattern.UNIX_LINES);

    private static final Pattern patternReachability1 =
            Pattern.compile("SOLUTION .+\n(.*)\npath cost: .+\n", Pattern.UNIX_LINES);

    private static final Pattern patternReachability2 =
            Pattern.compile("SOLUTION .+\n(.*)\n(.*)\ntotal cost of all paths: .+\n(\nConflict for signal (.+)\n)?", Pattern.UNIX_LINES);

    private static final Pattern patternNormalcy1 =
            Pattern.compile("SOLUTION .+\n(.*)\ntriggers: .+\n", Pattern.UNIX_LINES);

    public MpsatResultParser(ExternalProcessResult result) {
        String mpsatOutput;
        try {
            mpsatOutput = new String(result.getOutput(), "ISO-8859-1"); // iso-latin-1
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        solutions = new LinkedList<>();
        Matcher matcherReachability0 = patternReachability0.matcher(mpsatOutput);
        while (matcherReachability0.find()) {
            MpsatSolution solution = new MpsatSolution(null, null);
            solutions.add(solution);
        }
        Matcher matcherReachability1 = patternReachability1.matcher(mpsatOutput);
        while (matcherReachability1.find()) {
            Trace trace = getTrace(matcherReachability1.group(1));
            MpsatSolution solution = new MpsatSolution(trace, null);
            solutions.add(solution);
        }
        Matcher matcherRreachability2 = patternReachability2.matcher(mpsatOutput);
        while (matcherRreachability2.find()) {
            Trace mainTrace = getTrace(matcherRreachability2.group(1));
            Trace branchTrace = getTrace(matcherRreachability2.group(2));
            String signalName = matcherRreachability2.group(4);
            MpsatSolution solution = new MpsatSolution(mainTrace, branchTrace);
            solution.setComment(signalName);
            solutions.add(solution);
        }
        Matcher matcherNormalcy = patternNormalcy1.matcher(mpsatOutput);
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
