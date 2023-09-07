package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DiInterfaceParameters {

    private static final String EXCEPTION_SETS =
            "/* insert sets of input signal names to waive */"; // For example: {"in1", "in2"}, {"in3"}

    private static final String REACH_EXPRESSION =
            "// Checks whether the STG's interface is delay insensitive,\n" +
            "// i.e. an input transition cannot trigger another input transition.\n" +
            "// Note that a+ triggering a- is considered a violation; add {\"a\"} to EXCEPTION_SETS to waive.\n" +
            "card DUMMY != 0 ? fail \"Delay insensitive interface can be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    EXCEPTION_SETS = {" + EXCEPTION_SETS  + "{\"\"}} \\ {{\"\"}},\n" +
            "    EXCEPTIONS = gather set in EXCEPTION_SETS { S set },\n" +
            "    TRINP = T INPUTS * T EVENTS\n" +
            "{\n" +
            "    exists ti in TRINP {\n" +
            "        let\n" +
            "            pre_ti = pre ti,\n" +
            "            SUSPECTS = gather ti_trig in pre pre_ti * TRINP \\ {ti} s.t.\n" +
            "                ~is_empty((post ti_trig \\ pre ti_trig) * pre_ti)\n" +
            "                &\n" +
            "                ~exists set in EXCEPTIONS { S ti in set & S ti_trig in set }\n" +
            "            { ti_trig }\n" +
            "        {\n" +
            "            // Check if some ti_trig can trigger ti\n" +
            "            exists ti_trig in SUSPECTS {\n" +
            "                forall p in pre_ti \\ post ti_trig { $ p }\n" +
            "                &\n" +
            "                @ ti_trig\n" +
            "                &\n" +
            "                (S ti = S ti_trig ? ~@ ti : ~@S ti)\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private final Set<Set<String>> exceptionSignalSets;

    public DiInterfaceParameters(Collection<? extends Collection<String>> exceptionSignalSets) {
        this.exceptionSignalSets = exceptionSignalSets == null
                ? Collections.emptySet()
                : exceptionSignalSets.stream().map(HashSet::new).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DiInterfaceParameters) {
            return exceptionSignalSets.equals(((DiInterfaceParameters) other).exceptionSignalSets);
        }
        return false;
    }

    public Set<Set<String>> getExceptionSignalSets() {
        return Collections.unmodifiableSet(exceptionSignalSets);
    }

    public VerificationParameters getVerificationParameters() {
        String description = "Delay insensitive interface";
        if (!exceptionSignalSets.isEmpty()) {
            description += " with exceptions (" + exceptionSignalSets.stream()
                    .map(signals -> "{" + String.join(", ", signals) + "}")
                    .collect(Collectors.joining(", ")) + ")";
        }

        String replacement = exceptionSignalSets.stream()
                .map(signals -> "{" + signals.stream()
                        .map(signal -> "\"" + signal + "\"")
                        .collect(Collectors.joining(", ")) + "}, ")
                .collect(Collectors.joining());

        String reach = REACH_EXPRESSION.replace(EXCEPTION_SETS, replacement);

        return new VerificationParameters(description,
                VerificationMode.STG_REACHABILITY_DI_INTERFACE, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getDescriptiveSuffix() {
                return "Delay_insensitive_interface" + (exceptionSignalSets.isEmpty() ? "" : "_with_exceptions");
            }
        };
    }

}
