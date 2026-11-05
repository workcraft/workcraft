package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.types.Pair;
import org.workcraft.utils.SortUtils;

import java.util.*;
import java.util.stream.Collectors;

public class OutputPersistencyParameters {

    private static final String OUTPUT_PERSISTENCY_SKIP_NAMES_REPLACEMENT =
            "/* insert signal names that should be skipped */"; // For example: "scanout__1", "scanout__2",

    private static final String OUTPUT_PERSISTENCY_EXCEPTION_PAIRS_REPLACEMENT =
            "/* insert signal pairs of output persistency exceptions */"; // For example: ("me_g1", "me_g2"), ("me_g2", "me_g1"),

    private static final String OUTPUT_PERSISTENCY_REACH = """
            // Checks whether the STG is output-persistent, i.e. no local signal can be disabled by any other signal,
            // with the exception of the provided set of pairs of signals (e.g. mutex outputs).
            card DUMMY != 0 ? fail "Output persistency can currently be checked only for STGs without dummies" :
            let
                EXCEPTION_PAIRS = {%s("", "")} \\ {("", "")},
                SKIP_SIGNALS = S ( {%s""} \\ {""} ),
                EXCEPTION_SIGNALS = gather pair in EXCEPTION_PAIRS {
                    (S pair[0], S pair[1])
                },
                TR = T EVENTS,
                TRL = T (LOCAL \\ SKIP_SIGNALS) * TR,
                TRPT = gather t in TRL s.t. ~is_minus t { t },
                TRMT = gather t in TRL s.t. ~is_plus t { t }
            {
                exists t_loc in TRL {
                    let
                        pre_t_loc = pre t_loc,
                        OTHER_LOC = (T S t_loc \\ {t_loc}) * (is_plus t_loc ? TRPT : is_minus t_loc ? TRMT : TR) {
                        // Check if some t can disable t_loc without enabling any other transition labelled by S t_loc.
                        exists t in post pre_t_loc * TR s.t. S t != S t_loc &
                                ~((S t, S t_loc) in EXCEPTION_SIGNALS) & ~is_empty((pre t \\ post t) * pre_t_loc) {
                            forall t_loc1 in OTHER_LOC s.t. is_empty(pre t_loc1 * (pre t \\ post t)) {
                                exists p in pre t_loc1 \\ post t { ~$p }
                            }
                            &
                            @t
                        }
                        &
                        @t_loc
                    }
                }
            }
            """.formatted(OUTPUT_PERSISTENCY_EXCEPTION_PAIRS_REPLACEMENT, OUTPUT_PERSISTENCY_SKIP_NAMES_REPLACEMENT);

    public static VerificationParameters getOutputPersistencyParameters() {
        return getOutputPersistencyParameters(Collections.emptyList());
    }

    public static VerificationParameters getOutputPersistencyParameters(
            Collection<Pair<String, String>> exceptionPairs) {

        return getOutputPersistencyParameters(exceptionPairs, Collections.emptyList());
    }

    public static VerificationParameters getOutputPersistencyParameters(
            Collection<Pair<String, String>> exceptionPairs, Collection<String> skipSignals) {

        String exceptionPairsString = exceptionPairs.stream()
                .map(pair -> "(\"" + pair.getFirst() + "\", \"" + pair.getSecond() + "\"), ")
                .collect(Collectors.joining());

        String skipSignalsString = skipSignals.stream()
                .map(signal -> "\"" + signal + "\", ")
                .collect(Collectors.joining());

        String reachOutputPersistence = OUTPUT_PERSISTENCY_REACH
                .replace(OUTPUT_PERSISTENCY_EXCEPTION_PAIRS_REPLACEMENT, exceptionPairsString)
                .replace(OUTPUT_PERSISTENCY_SKIP_NAMES_REPLACEMENT, skipSignalsString);

        return new VerificationParameters("Output persistency",
                VerificationMode.STG_REACHABILITY_OUTPUT_PERSISTENCY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reachOutputPersistence, true);
    }

    private final Set<TreeSet<String>> exceptionSignalSets;
    private final Set<String> skipSignals;

    public OutputPersistencyParameters(
            Collection<? extends Collection<String>> exceptionSignalSets, Collection<String> skipSignals) {

        this.exceptionSignalSets = (exceptionSignalSets == null) ? Collections.emptySet()
                : exceptionSignalSets.stream().map(TreeSet::new).collect(Collectors.toSet());

        this.skipSignals = (skipSignals == null) ? Collections.emptySet() : new HashSet<>(skipSignals);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof OutputPersistencyParameters otherOutputPersistencyParameters) {
            return exceptionSignalSets.equals(otherOutputPersistencyParameters.exceptionSignalSets)
                    && skipSignals.equals(otherOutputPersistencyParameters.skipSignals);
        }
        return false;
    }

    public List<TreeSet<String>> getOrderedExceptionSignalSets() {
        return SortUtils.getSortedNatural(exceptionSignalSets, s -> String.join(" ", s));
    }

    public boolean isException(Set<String> signals) {
        return exceptionSignalSets.stream()
                .anyMatch(signalSet -> signalSet.containsAll(signals));
    }

    public VerificationParameters getVerificationParameters() {
        String description = "Delay insensitive interface";
        List<TreeSet<String>> orderedExceptionSignalSets = getOrderedExceptionSignalSets();
        if (!orderedExceptionSignalSets.isEmpty()) {
            description += " with exceptions (" + orderedExceptionSignalSets.stream()
                    .map(signals -> "{" + String.join(", ", signals) + "}")
                    .collect(Collectors.joining(", ")) + ")";
        }

        String replacement = orderedExceptionSignalSets.stream()
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
