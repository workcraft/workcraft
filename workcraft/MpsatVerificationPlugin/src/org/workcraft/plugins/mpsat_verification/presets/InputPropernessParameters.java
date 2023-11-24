package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.utils.SortUtils;

import java.util.*;
import java.util.stream.Collectors;

public class InputPropernessParameters {

    private static final String EXCEPTION_NAMES =
            "/* insert names of output and internal signals to waive */"; // For example: "out1", "sig2"

    private static final String REACH_EXPRESSION =
            "// Checks whether the STG is input proper, i.e. no input can be triggered by an internal signal or disabled by a local signal.\n" +
            "card DUMMY != 0 ? fail \"Input properness can currently be checked only for STGs without dummies\" :\n" +
            "let\n" +
            "    EXCEPTION_NAMES = {" + EXCEPTION_NAMES + "\"\"} \\ {\"\"},\n" +
            "    EXCEPTIONS = S EXCEPTION_NAMES,\n" +
            "    TR = T EVENTS,\n" +
            "    TRINP = T (INPUTS \\ EXCEPTIONS) * TR,\n" +
            "    TRI = T INTERNAL * TR,\n" +
            "    TRL = T LOCAL * TR,\n" +
            "    TRPT = gather t in TRINP s.t. ~is_minus t { t },\n" +
            "    TRMT = gather t in TRINP s.t. ~is_plus t { t }\n" +
            "{\n" +
            "    exists t_inp in TRINP {\n" +
            "        let\n" +
            "            pre_t_inp = pre t_inp,\n" +
            "            OTHER_INP = (T S t_inp \\ {t_inp}) * (is_plus t_inp ? TRPT : is_minus t_inp ? TRMT : TR) {\n" +
            "            // Check if some t_int can trigger t_inp.\n" +
            "            exists t_int in pre pre_t_inp * TRI s.t. ~is_empty((post t_int \\ pre t_int) * pre_t_inp) {\n" +
            "                forall p in pre_t_inp \\ post t_int { $p }\n" +
            "                &\n" +
            "                @t_int\n" +
            "            }\n" +
            "            &\n" +
            "            ~@S t_inp\n" +
            "            |\n" +
            "            // Check if some t_loc can disable t_inp without enabling any other transition labelled by S t_inp.\n" +
            "            exists t_loc in post pre_t_inp * TRL s.t. ~is_empty((pre t_loc \\ post t_loc) * pre_t_inp) {\n" +
            "                forall t_inp1 in OTHER_INP s.t. is_empty(pre t_inp1 * (pre t_loc \\ post t_loc)) {\n" +
            "                    exists p in pre t_inp1 \\ post t_loc { ~$p }\n" +
            "                }\n" +
            "                &\n" +
            "                @t_loc\n" +
            "            }\n" +
            "            &\n" +
            "            @t_inp\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private final Set<String> exceptionSignals;

    public InputPropernessParameters(Collection<String> exceptionSignals) {
        this.exceptionSignals = exceptionSignals == null ? Collections.emptySet() : new HashSet<>(exceptionSignals);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof InputPropernessParameters) {
            return exceptionSignals.equals(((InputPropernessParameters) other).exceptionSignals);
        }
        return false;
    }

    public List<String> getOrderedExceptionSignals() {
        return SortUtils.getSortedNatural(exceptionSignals);
    }

    public VerificationParameters getVerificationParameters() {
        String description = "Input properness";
        List<String> orderedExceptionSignals = getOrderedExceptionSignals();
        if (!orderedExceptionSignals.isEmpty()) {
            description += " with exceptions (" + String.join(", ", orderedExceptionSignals) + ")";
        }

        String replacement = orderedExceptionSignals.stream()
                .map(signal -> "\"" + signal + "\", ")
                .collect(Collectors.joining());

        String reach = REACH_EXPRESSION.replace(EXCEPTION_NAMES, replacement);

        return new VerificationParameters(description,
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getDescriptiveSuffix() {
                return "Input_properness" + (exceptionSignals.isEmpty() ? "" : "_with_exceptions");
            }
        };
    }

}
