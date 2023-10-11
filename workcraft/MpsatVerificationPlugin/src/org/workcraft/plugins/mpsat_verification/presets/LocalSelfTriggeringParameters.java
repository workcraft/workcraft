package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.utils.SortUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalSelfTriggeringParameters {

    private static final String EXCEPTION_NAMES =
            "/* insert names of output and internal signals to waive */"; // For example: "out1", "sig2"

    private static final String REACH_EXPRESSION =
            "// Checks whether the STG is free of self-triggering output and internal signals.\n" +
            "card DUMMY != 0 ? fail \"Absence of local self-triggering can currently be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    EXCEPTION_NAMES = {" + EXCEPTION_NAMES + "\"\"} \\ {\"\"},\n" +
            "    EXCEPTIONS = S EXCEPTION_NAMES\n" +
            "{\n" +
            "    exists s in LOCAL \\ EXCEPTIONS {\n" +
            "        let s_tran=T s {\n" +
            "            exists t1 in s_tran, t2 in s_tran s.t.\n" +
            "                t1!=t2\n" +
            "                &\n" +
            "                ~ is_empty (pre t2 * (post t1 \\ pre t1))  // t1 structurally triggers t2\n" +
            "                &\n" +
            "                is_empty (pre t2 * (pre t1 \\ post t1))    // t2 is not disabled by t1\n" +
            "            {\n" +
            "                @t1 & ~@t2\n" +
            "                &\n" +
            "                forall p in pre t2 s.t. ~(p in post t1) {\n" +
            "                    ~(p in pre t1) & $p\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private final Set<String> exceptionSignals;

    public LocalSelfTriggeringParameters(Collection<String> exceptionSignals) {
        this.exceptionSignals = exceptionSignals == null ? Collections.emptySet() : new HashSet<>(exceptionSignals);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof LocalSelfTriggeringParameters) {
            return exceptionSignals.equals(((LocalSelfTriggeringParameters) other).exceptionSignals);
        }
        return false;
    }

    public boolean isException(String signal) {
        return exceptionSignals.contains(signal);
    }

    public List<String> getOrderedExceptionSignals() {
        return SortUtils.getSortedNatural(exceptionSignals);
    }

    public VerificationParameters getVerificationParameters() {
        String description = "Absence of local self-triggering";
        List<String> orderedExceptionSignals = getOrderedExceptionSignals();
        if (!orderedExceptionSignals.isEmpty()) {
            description += " with exceptions (" + String.join(", ", orderedExceptionSignals) + ")";
        }

        String replacement = orderedExceptionSignals.stream()
                .map(signal -> "\"" + signal + "\", ")
                .collect(Collectors.joining());

        String reach = REACH_EXPRESSION.replace(EXCEPTION_NAMES, replacement);

        return new VerificationParameters(description,
                VerificationMode.STG_REACHABILITY_LOCAL_SELF_TRIGGERING, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getDescriptiveSuffix() {
                return "Absence_of_local_self_triggering" + (exceptionSignals.isEmpty() ? "" : "_with_exceptions");
            }
        };
    }

}
