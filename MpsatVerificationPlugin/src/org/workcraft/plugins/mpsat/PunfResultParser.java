package org.workcraft.plugins.mpsat;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.Trace;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.util.Pair;

public class PunfResultParser {

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

    private final Pair<MpsatSolution, Cause> outcome;

    /*
     * \R -- any Unicode linebreak sequence introduced in Java 8.
     * It is equivalent to \u000D\u000A\u000A\u000B\u000C\u000D\u0085\u2028\u2029].
     */

    private static final Pattern patternInconsistent = Pattern.compile(
            "Error: the STG is inconsistent, signal (.*); trace:\\R" +
            "(.*)\\R",
            Pattern.UNIX_LINES);

    private static final Pattern patternNotSafe = Pattern.compile(
            "Error: the net is not safe, place (.*); trace:\\R" +
            "(.*)\\R",
            Pattern.UNIX_LINES);

    private static final Pattern patternEmptyPreset = Pattern.compile(
            "Error: the net contains (.*) transition\\(s\\) with empty preset: (.*)",
            Pattern.UNIX_LINES);

    public PunfResultParser(ExternalProcessResult result) {
        String punfOutput;
        try {
            punfOutput = new String(result.getErrors(), "ISO-8859-1"); // iso-latin-1
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        Matcher matcherInconsistent = patternInconsistent.matcher(punfOutput);
        Matcher matcherNotSafe = patternNotSafe.matcher(punfOutput);
        Matcher matcherEmptyPreset = patternEmptyPreset.matcher(punfOutput);
        if (matcherInconsistent.find()) {
            String comment = "Signal '" + matcherInconsistent.group(1) + "' is inconsistent";
            Trace trace = getTrace(matcherInconsistent.group(2));
            MpsatSolution solution = new MpsatSolution(trace, null, comment);
            outcome = Pair.of(solution, Cause.INCONSISTENT);
        } else if (matcherNotSafe.find()) {
            String comment = "Place '" + matcherNotSafe.group(1) + "' is unsafe";
            Trace trace = getTrace(matcherNotSafe.group(2));
            MpsatSolution solution = new MpsatSolution(trace, null, comment);
            outcome = Pair.of(solution, Cause.NOT_SAFE);
        } else if (matcherEmptyPreset.find()) {
            String comment = "Transition(s) with empty preset: " + matcherEmptyPreset.group(2);
            MpsatSolution solution = new MpsatSolution(null, null, comment);
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

    public Pair<MpsatSolution, Cause> getOutcome() {
        return outcome;
    }

}
