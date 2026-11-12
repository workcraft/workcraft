package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.types.Pair;
import org.workcraft.utils.SortUtils;

import java.util.*;
import java.util.stream.Collectors;

public class OutputPersistencyParameters {

    private static final String SKIP_NAMES_REPLACEMENT =
            "/* insert signal names that should be skipped */"; // For example: "scanout__1", "scanout__2",

    private static final String EXCEPTION_PAIRS_REPLACEMENT =
            "/* insert signal pairs of output persistency exceptions */"; // For example: ("me_g1", "me_g2"), ("me_g2", "me_g1"),

    private static final String REACH_EXPRESSION = """
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
            """.formatted(EXCEPTION_PAIRS_REPLACEMENT, SKIP_NAMES_REPLACEMENT);

    private final Collection<TreeSet<String>> exceptionSignalSets;

    public OutputPersistencyParameters(Collection<? extends Collection<String>> exceptionSignalSets) {
        this.exceptionSignalSets = (exceptionSignalSets == null) ? Collections.emptySet()
                : exceptionSignalSets.stream().map(TreeSet::new).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof OutputPersistencyParameters otherOutputPersistencyParameters) {
            return exceptionSignalSets.equals(otherOutputPersistencyParameters.exceptionSignalSets);
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

    public VerificationParameters getVerificationParameters(Collection<Pair<String, String>> mutexGrantPairs) {
        return getVerificationParameters(mutexGrantPairs, Collections.emptyList(), exceptionSignalSets);
    }

    public VerificationParameters getVerificationParameters(Collection<Pair<String, String>> mutexGrantPairs,
            Collection<String> skipSignals) {

        return getVerificationParameters(mutexGrantPairs, skipSignals, exceptionSignalSets);
    }

    public static VerificationParameters getVerificationParameters(Collection<Pair<String, String>> mutexGrantPairs,
            Collection<String> skipSignals, Collection<? extends Collection<String>> exceptionSignalSets) {

        // Mutex grants and skip signals are implied by the model and the settings, so only user-defined are reported
        String description = "Output persistency";
        if (!exceptionSignalSets.isEmpty()) {
            description += " with exceptions (" + exceptionSignalSets.stream()
                    .map(TreeSet::new)
                    .sorted(Comparator.comparing(signals -> String.join(" ", signals)))
                    .map(signals -> "{" + String.join(", ", signals) + "}")
                    .collect(Collectors.joining(", ")) + ")";
        }

        StringBuilder sb = new StringBuilder();
        for (Pair<String, String> pair : mutexGrantPairs) {
            sb.append(genReachPair(pair.getFirst(), pair.getSecond()));
        }
        for (Collection<String> exceptionSignalSet : exceptionSignalSets) {
            for (String first : exceptionSignalSet) {
                for (String second : exceptionSignalSet) {
                    if (!first.equals(second)) {
                        sb.append(genReachPair(first, second));
                    }
                }
            }
        }
        String exceptionPairsString = sb.toString();

        String skipSignalsString = skipSignals.stream()
                .map(signal -> "\"" + signal + "\", ")
                .collect(Collectors.joining());

        String reach = REACH_EXPRESSION
                .replace(EXCEPTION_PAIRS_REPLACEMENT, exceptionPairsString)
                .replace(SKIP_NAMES_REPLACEMENT, skipSignalsString);

        return new VerificationParameters(description,
                VerificationMode.STG_REACHABILITY_OUTPUT_PERSISTENCY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getDescriptiveSuffix() {
                return "Output_persistency" + (exceptionSignalSets.isEmpty() ? "" : "_with_exceptions");
            }
        };
    }

    private static String genReachPair(String first, String second) {
        return "(\"" + first + "\", \"" + second + "\"), ";
    }

}
