package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.types.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ReachUtils {

    public static VerificationParameters getToolchainPreparationParameters() {
        return new VerificationParameters("Toolchain preparation of data",
                VerificationMode.UNDEFINED, 0,
                null, 0);
    }

    public static VerificationParameters getToolchainCompletionParameters() {
        return new VerificationParameters("Toolchain completion",
                VerificationMode.UNDEFINED, 0,
                null, 0);
    }

    public static VerificationParameters getDeadlockParameters() {
        return new VerificationParameters("Deadlock freeness",
                VerificationMode.DEADLOCK, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount());
    }

    private static final String CONSISTENCY_REACH = """
            // Checks whether the STG is consistent, i.e. rising and falling transitions of every signal
            // alternate in all traces
            exists s in SIGNALS \\ DUMMY {
                let Es = E s {
                    $s & exists e in Es s.t. is_plus e { @e }
                    |
                    ~$s & exists e in Es s.t. is_minus e { @e }
                }
            }
            """;

    public static final String CONSISTENCY_PROPERTY_DESCRIPTION = "Consistency";

    public static VerificationParameters getConsistencyParameters() {
        return new VerificationParameters("Consistency",
                VerificationMode.STG_REACHABILITY_CONSISTENCY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                CONSISTENCY_REACH, true);
    }

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

    private static final String SHADOW_ENABLER_PLACE_REPLACEMENT =
            "/* insert the place name that enables all shadow transitions here */"; // For example: "shadow_place"

    private static final String CONFORMATION_REACH = """
            // Check whether several STGs conform to each other.
            // The enabled-via-dummies semantics is assumed for @.
            // Configurations with maximal dummies are assumed to be allowed.
            let
                // Shadow output transitions in the composed STG.
                SHADOW_OUTPUT_TRANSITIONS = post P "%s"
            {
                // Optimisation: make sure phantom events are not in the configuration.
                forall e in E SHADOW_OUTPUT_TRANSITIONS \\ CUTOFFS { ~$e }
                &
                // Check if some output signal is enabled due to phantom transitions only;
                // this would mean that some component STG does not conform to the rest of the composition.
                exists o in OUTPUTS {
                    let tran_o = T o {
                        exists t in tran_o * SHADOW_OUTPUT_TRANSITIONS {
                            forall p in pre t { $p }
                        }
                        &
                        forall tt in tran_o \\ SHADOW_OUTPUT_TRANSITIONS { ~@tt }
                    }
                }
            }
            """.formatted(SHADOW_ENABLER_PLACE_REPLACEMENT);

    public static VerificationParameters getConformationParameters(String shadowEnablerPlaceRef) {
        String reach = CONFORMATION_REACH.replace(SHADOW_ENABLER_PLACE_REPLACEMENT, shadowEnablerPlaceRef);
        return new VerificationParameters("Conformation",
                VerificationMode.STG_REACHABILITY_CONFORMATION, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

    public static VerificationParameters getNwayConformationParameters(String shadowEnablerPlaceRef) {
        String reach = CONFORMATION_REACH.replace(SHADOW_ENABLER_PLACE_REPLACEMENT, shadowEnablerPlaceRef);
        return new VerificationParameters("N-way conformation",
                VerificationMode.STG_REACHABILITY_CONFORMATION, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

    // REACH expression for checking if these two pairs of signals can be implemented by a mutex
    private static final String MUTEX_PROTOCOL_REPLACEMENT = "/* insert true or false for late protocol here */";
    private static final String MUTEX_R1_REPLACEMENT = "/* insert r1 name here */";
    private static final String MUTEX_G1_REPLACEMENT = "/* insert g1 name here */";
    private static final String MUTEX_R2_REPLACEMENT = "/* insert r2 name here */";
    private static final String MUTEX_G2_REPLACEMENT = "/* insert g2 name here */";

    private static final String MUTEX_PROTOCOL_REACH = """
            // For given signals r1, r2, g1, g2, check whether they follow
            // the early or late mutex protocol mutex with requests r1/r2 and grants g1/g2.
            // The properties to check are:
            //   1. The mutex has a sensible initial state:
            //      (r1<->g1) & (r2<->g2) & (~r1 | ~r2) in the initial state
            //   2. The constraints on the values of the next state functions
            //      which depend on whether the protocol is early or late.
            // Note that the mutual exclusion of the critical sections:
            //   ~((r1 & g1) & (r2 & g2))
            // follows from these constraints inductively, as it is implied
            // in the initial state by the first constraint, and the constraints
            // on the next state function ensure that it is preserved.
            let
                is_late_protocol = %s,
                r1s = S"%s",
                g1s = S"%s",
                r2s = S"%s",
                g2s = S"%s",
                r1i = is_init r1s,
                g1i = is_init g1s,
                r2i = is_init r2s,
                g2i = is_init g2s,
                r1 = $r1s,
                g1 = $g1s,
                r2 = $r2s,
                g2 = $g2s,
                g1nxt = 'g1s,
                g2nxt = 'g2s
            {
                (r1i ^ g1i) | (r2i ^ g2i) | (r1i & r2i) // bad initial state
                |
                (is_late_protocol ?
                    r1 & ~g2 & ~g1nxt  // negation of r1&~g2 => nxt(g1)
                    |
                    ~r1 & g1nxt        // negation of ~r1 => ~nxt(g1)
                    |
                    r2 & g2 & g1nxt    // negation of r2&g2 => ~nxt(g1)
                    |
                    r2 & ~g1 & ~g2nxt  // negation of r2&~g1 => nxt(g2)
                    |
                    ~r2 & g2nxt        // negation of ~r2 => ~nxt(g2)
                    |
                    r1 & g1 & g2nxt    // negation of r1&g1 => ~nxt(g2)
                :
                    (g1nxt ^ (r1 & (~r2 | ~g2)))  // negation of nxt(g1) := r1 & (~r2 | ~g2)
                    |
                    (g2nxt ^ (r2 & (~r1 | ~g1)))  // negation of nxt(g2) := r2 & (~r1 | ~g1)
                )
            }""".formatted(MUTEX_PROTOCOL_REPLACEMENT,
            MUTEX_R1_REPLACEMENT, MUTEX_G1_REPLACEMENT, MUTEX_R2_REPLACEMENT, MUTEX_G2_REPLACEMENT);

    public static List<VerificationParameters> getMutexProtocolParameters(Collection<Mutex> mutexes) {
        return mutexes.stream().map(ReachUtils::getMutexProtocolParameters).collect(Collectors.toList());
    }

    private static VerificationParameters getMutexProtocolParameters(Mutex mutex) {
        String reach = MUTEX_PROTOCOL_REACH
                .replace(MUTEX_PROTOCOL_REPLACEMENT, mutex.protocol == Mutex.Protocol.LATE ? "true" : "false")
                .replace(MUTEX_R1_REPLACEMENT, mutex.r1.name)
                .replace(MUTEX_G1_REPLACEMENT, mutex.g1.name)
                .replace(MUTEX_R2_REPLACEMENT, mutex.r2.name)
                .replace(MUTEX_G2_REPLACEMENT, mutex.g2.name);

        String description = "Mutex "
                + (mutex.protocol == Mutex.Protocol.LATE ? "late" : "early")
                + " protocol for place " + MutexUtils.getMutexPlaceExtendedTitle(mutex);

        return new VerificationParameters(description,
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getDescriptiveSuffix() {
                return "-Mutex_protocol-" + mutex.name;
            }
        };
    }

    public static String getBooleanAsString(boolean value) {
        return value ? "true" : "false";
    }

}
