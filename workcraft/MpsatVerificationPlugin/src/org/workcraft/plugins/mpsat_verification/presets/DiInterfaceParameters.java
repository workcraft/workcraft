package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.utils.SortUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DiInterfaceParameters {

    private static final String EXCEPTION_SETS =
            "/* insert sets of input signal names to waive */"; // For example: {"in1", "in2"}, {"in3"}

    private static final String REACH_EXPRESSION = """
            // Checks whether the STG's interface is delay insensitive, i.e. in any reachable state, if the
            // inputs can be accepted in some order then they can be accepted in the opposite order too.
            // Violations are detected on the level of signals (not transitions) in two cases:
            //  - an input transition triggers another input transition (the latter is not accepted
            //    before the former), and
            //  - an input transition disables another input (e.g. via a read arc) without any other
            //    transition of the latter's signal becoming/remaining enabled, while the opposite order is accepted.
            // The STG is assumed to be safe and consistent.
            // To exempt signals from the check, list sets of their names in EXCEPTION_SETS, e.g.
            //   EXCEPTION_SETS = {{"i0"}, {"i1"}, {""}} \\ {{""}}
            // then signals within the same set are not considered to violate the property w.r.t. each other
            // (e.g. an input triggering itself is not a violation).
            card DUMMY != 0 ? fail "Delay insensitive interface can be checked only for STGs without dummies" :
            let
                EXCEPTION_SETS = {%s{""}} \\ {{""}},
                EXCEPTIONS = gather set in EXCEPTION_SETS { S set },
                TR = T EVENTS,
                TRINP = T INPUTS * TR,
                // Transitions that can fire in the same state as a given one, i.e. are not of opposite polarity
                TRPT = gather t in TR s.t. ~is_minus t { t },
                TRMT = gather t in TR s.t. ~is_plus t { t }
            {
                exists ti in TRINP {
                    let
                        pre_ti = pre ti,
                        // Places that ti consumes without putting a token back
                        CONS_ti = pre ti \\ post ti,
                        SUSPECTS = gather ti_trig in pre pre_ti * TRINP \\ {ti} s.t.
                            // Trigger does not steal tokens from input transition preset
                            is_empty ((pre ti_trig \\ post ti_trig) * pre_ti)
                            &
                            // Trigger puts new tokens into input transition preset
                            ~is_empty((post ti_trig \\ pre ti_trig) * pre_ti)
                            &
                            // Signals of trigger and input transition are not in exception sets
                            ~exists set in EXCEPTIONS { S ti in set & S ti_trig in set }
                        { ti_trig },
                        // Inputs of other signals that need a place consumed by ti
                        DISABLED = gather ti_dis in post CONS_ti * TRINP s.t.
                            S ti_dis != S ti
                            &
                            ~exists set in EXCEPTIONS { S ti in set & S ti_dis in set }
                        { ti_dis },
                        // Other transitions of the signal of ti that may be enabled together with ti
                        OTHER_ti = (T S ti \\ {ti}) * (is_plus ti ? TRPT : is_minus ti ? TRMT : TR)
                    {
                        // Some ti_trig can trigger ti
                        exists ti_trig in SUSPECTS {
                            forall p in pre_ti \\ post ti_trig { $ p }
                            &
                            @ ti_trig
                            &
                            (S ti = S ti_trig ? ~@ ti : ~@S ti)
                        }
                        |
                        // ti and ti_dis are both enabled; after ti_dis the signal of ti is
                        // still enabled, but after ti no transition of the signal of ti_dis is
                        exists ti_dis in DISABLED {
                            let
                                CONS_dis = pre ti_dis \\ post ti_dis,
                                // Other transitions of the signal of ti_dis that may be enabled together with ti_dis
                                OTHER_dis = (T S ti_dis \\ {ti_dis}) * (is_plus ti_dis ? TRPT : is_minus ti_dis ? TRMT : TR)
                            {
                                @ ti & @ ti_dis
                                &
                                // The signal of ti stays enabled after ti_dis: either ti itself
                                // keeps its tokens or another transition of the signal gets enabled
                                (is_empty (pre_ti * CONS_dis)
                                 |
                                 exists tx in OTHER_ti s.t. is_empty (pre tx * CONS_dis) {
                                     forall p in pre tx \\ post ti_dis { $ p }
                                 })
                                &
                                // No transition of the signal of ti_dis is enabled after ti;
                                // ti_dis itself is disabled by ti, and those that lose a token to ti are too
                                forall tx in OTHER_dis s.t. is_empty (pre tx * CONS_ti) {
                                    exists p in pre tx \\ post ti { ~$ p }
                                }
                            }
                        }
                    }
                }
            }
            """.formatted(EXCEPTION_SETS);

    private final Set<TreeSet<String>> exceptionSignalSets;

    public DiInterfaceParameters(Collection<? extends Collection<String>> exceptionSignalSets) {
        this.exceptionSignalSets = exceptionSignalSets == null
                ? Collections.emptySet()
                : exceptionSignalSets.stream().map(TreeSet::new).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DiInterfaceParameters) {
            return exceptionSignalSets.equals(((DiInterfaceParameters) other).exceptionSignalSets);
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
