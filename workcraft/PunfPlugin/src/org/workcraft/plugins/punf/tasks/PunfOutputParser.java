package org.workcraft.plugins.punf.tasks;

import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunfOutputParser {

    public enum Cause {
        INCONSISTENT("inconsistent"),
        NOT_SAFE("not safe"),
        EMPTY_PRESET("empty preset");

        private final String name;

        Cause(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Pair<Solution, Cause> outcome;

    /*
     * \R -- any Unicode linebreak sequence introduced in Java 8.
     * It is equivalent to \u000D\u000A\u000A\u000B\u000C\u000D\u0085\u2028\u2029].
     */

    private static final Pattern INCONSISTENT_PATTERN = Pattern.compile(
            "Error: the STG is inconsistent, signal (.*); trace:\\R" +
            "(.*)\\R",
            Pattern.UNIX_LINES);

    private static final Pattern NOT_SAFE_PATTERN = Pattern.compile(
            "Error: the net is not safe, place (.*); trace:\\R" +
            "(.*)\\R",
            Pattern.UNIX_LINES);

    private static final Pattern EMPTY_PRESET_PATTERN = Pattern.compile(
            "Error: the net contains (.*) transition\\(s\\) with empty preset: (.*)",
            Pattern.UNIX_LINES);

    public PunfOutputParser(PunfOutput output) {
        String stderr = output == null ? "" : output.getStderrString();
        Matcher matcherInconsistent = INCONSISTENT_PATTERN.matcher(stderr);
        Matcher matcherNotSafe = NOT_SAFE_PATTERN.matcher(stderr);
        Matcher matcherEmptyPreset = EMPTY_PRESET_PATTERN.matcher(stderr);
        if (matcherInconsistent.find()) {
            Solution solution = new Solution(getTrace(matcherInconsistent.group(2)));
            solution.setComment("Signal '" + matcherInconsistent.group(1) + "' is inconsistent");
            outcome = Pair.of(solution, Cause.INCONSISTENT);
        } else if (matcherNotSafe.find()) {
            Solution solution = new Solution(getTrace(matcherNotSafe.group(2)));
            solution.setComment("Place '" + matcherNotSafe.group(1) + "' is unsafe");
            outcome = Pair.of(solution, Cause.NOT_SAFE);
        } else if (matcherEmptyPreset.find()) {
            Solution solution = new Solution(new Trace());
            solution.setComment("Transition(s) with empty preset: " + matcherEmptyPreset.group(2));
            outcome = Pair.of(solution, Cause.EMPTY_PRESET);
        } else {
            outcome = null;
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

    public Pair<Solution, Cause> getOutcome() {
        return outcome;
    }

}
