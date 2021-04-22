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
     * It is equivalent to [\u000D\u000A\u000A\u000B\u000C\u000D\u0085\u2028\u2029].
     */

    private static final Pattern INCONSISTENT_ALTERNATE_PATTERN = Pattern.compile(
            "Error: the STG is inconsistent, signal (.*); its rising and falling edges do not alternate in trace:\\R" +
                    "(.*)\\R",
            Pattern.UNIX_LINES);

    private static final Pattern INCONSISTENT_CONFLICT_PATTERN = Pattern.compile(
            "Error: the STG is inconsistent, signal (.*); its initial value cannot be assigned due to traces:\\R" +
                    "(.*)\\R(.*)\\R",
            Pattern.UNIX_LINES);

    private static final Pattern INCONSISTENT_INITIAL_PATTERN = Pattern.compile(
            "Error: the STG is inconsistent, signal (.*); it is declared with the initial value (.*) conflicting with the trace:\\R" +
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
        Matcher inconsistentAlternateMatcher = INCONSISTENT_ALTERNATE_PATTERN.matcher(stderr);
        Matcher inconsistentConflictMatcher = INCONSISTENT_CONFLICT_PATTERN.matcher(stderr);
        Matcher inconsistentInitialMatcher = INCONSISTENT_INITIAL_PATTERN.matcher(stderr);
        Matcher notSafeMatcher = NOT_SAFE_PATTERN.matcher(stderr);
        Matcher emptyPresetMatcher = EMPTY_PRESET_PATTERN.matcher(stderr);
        if (inconsistentAlternateMatcher.find()) {
            Solution solution = new Solution(getTrace(inconsistentAlternateMatcher.group(2)));
            solution.setComment("Rising and falling edges of signal '" + inconsistentAlternateMatcher.group(1) + "' do not alternate in trace");
            outcome = Pair.of(solution, Cause.INCONSISTENT);
        } else if (inconsistentConflictMatcher.find()) {
            Trace mainTrace = getTrace(inconsistentConflictMatcher.group(2));
            Trace branchTrace = getTrace(inconsistentConflictMatcher.group(3));
            Solution solution = new Solution(mainTrace, branchTrace);
            solution.setComment("Initial value of signal '" + inconsistentConflictMatcher.group(1) + "' cannot be assigned due to conflicting traces");
            outcome = Pair.of(solution, Cause.INCONSISTENT);
        } else if (inconsistentInitialMatcher.find()) {
            Solution solution = new Solution(getTrace(inconsistentInitialMatcher.group(3)));
            solution.setComment("Initial value of signal '" + inconsistentInitialMatcher.group(1) + "' conflicts with trace");
            outcome = Pair.of(solution, Cause.INCONSISTENT);
        } else if (notSafeMatcher.find()) {
            Solution solution = new Solution(getTrace(notSafeMatcher.group(2)));
            solution.setComment("Place '" + notSafeMatcher.group(1) + "' is unsafe");
            outcome = Pair.of(solution, Cause.NOT_SAFE);
        } else if (emptyPresetMatcher.find()) {
            Solution solution = new Solution(new Trace());
            solution.setComment("Transition(s) with empty preset: " + emptyPresetMatcher.group(2));
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
