package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;

import java.util.*;
import java.util.stream.Collectors;

public class HandshakeParameters {

    public static final String VIOLATION_PREFIX = "Handshake protocol is violated: ";

    public enum State {
        REQ0ACK0(false, false),
        REQ1ACK0(true, false),
        REQ1ACK1(true, true),
        REQ0ACK1(false, true);

        private final boolean reqState;
        private final boolean ackState;

        State(boolean reqState, boolean ackState) {
            this.reqState = reqState;
            this.ackState = ackState;
        }

        public boolean isReqAsserted() {
            return reqState;
        }

        public boolean isAckAsserted() {
            return ackState;
        }
    }

    private static final String REQ_NAMES_REPLACEMENT =
            "/* insert request signal names here */"; // For example: "req1", "req2"

    private static final String ACK_NAMES_REPLACEMENT =
            "/* insert acknowledgement signal names here */"; // For example: "ack1", "ack2"

    private static final String CHECK_ASSERT_ENABLED_REPLACEMENT =
            "/* insert check assert enabled flag here */"; // true or false

    private static final String CHECK_WITHDRAW_ENABLED_REPLACEMENT =
            "/* insert check withdraw enabled flag here */"; // true or false

    private static final String REQ_INITIALLY_ASSERTED_REPLACEMENT =
            "/* insert request initial assertion state here */"; // true or false

    private static final String ACK_INITIALLY_ASSERTED_REPLACEMENT =
            "/* insert acknowledgment initial assertion state here */"; // true or false

    private static final String INITIAL_HANDSHAKE_REACH = """
            // Handshake protocol initial state check.
            let
                // Non-empty set of request signals.
                REQS = S {%s},
                // Non-empty set of acknowledgement signals.
                ACKS = S {%s},
                // The following two values specify the initial state of the handshake.
                REQ_INITIALLY_ASSERTED = %s,
                ACK_INITIALLY_ASSERTED = %s
            {
                // Check that either inversions are allowed or the initial values of requests are equal to REQ_INITIALLY_ASSERTED.
                exists req in REQS { is_init req ^ REQ_INITIALLY_ASSERTED }
                |
                // Check that either inversions are allowed or the initial values of acknowledgements are equal to ACK_INITIALLY_ASSERTED.
                exists ack in ACKS { is_init ack ^ ACK_INITIALLY_ASSERTED }
            }
            """.formatted(REQ_NAMES_REPLACEMENT, ACK_NAMES_REPLACEMENT, REQ_INITIALLY_ASSERTED_REPLACEMENT, ACK_INITIALLY_ASSERTED_REPLACEMENT);

    private static final String ONEHOT_HANDSHAKE_REACH = """
            // Handshake protocol one-hotness check.
            let
                // Non-empty set of request signals.
                REQS = S {%s},
                // Non-empty set of acknowledgement signals.
                ACKS = S {%s},
                // The following two values specify the initial state of the handshake.
                REQ_INITIALLY_ASSERTED = %s,
                ACK_INITIALLY_ASSERTED = %s
            {
                // Check that the requests are 1-hot (correcting for the polarity and initial state of handshake).
                threshold req in REQS { $ req ^ (is_init req ^ REQ_INITIALLY_ASSERTED) }
                |
                // Check that the acknowledgement are 1-hot (correcting for the polarity and initial state of handshake).
                threshold ack in ACKS { $ ack ^ (is_init ack ^ ACK_INITIALLY_ASSERTED) }
            }
            """.formatted(REQ_NAMES_REPLACEMENT, ACK_NAMES_REPLACEMENT,
            REQ_INITIALLY_ASSERTED_REPLACEMENT, ACK_INITIALLY_ASSERTED_REPLACEMENT);


    private static final String ORDERING_HANDSHAKE_REACH = """
            // Handshake protocol ordering check.
            let
                // Non-empty set of request signals.
                REQS = S {%s},
                // Non-empty set of acknowledgement signals.
                ACKS = S {%s},
                // The following two values specify the initial state of the handshake.
                REQ_INITIALLY_ASSERTED = %s,
                ACK_INITIALLY_ASSERTED = %s,

                // Some request is asserted (correcting for the polarity and initial state of handshake).
                req = exists s in REQS { $ s ^ (is_init s ^ REQ_INITIALLY_ASSERTED) },
                // Some acknowledgement is asserted (correcting for the polarity and initial state of handshake).
                ack = exists s in ACKS { $ s ^ (is_init s ^ ACK_INITIALLY_ASSERTED) },

                // Request assert/withdraw/change is enabled (correcting for the polarity and initial state of handshake).
                en_req_assert = exists e in E REQS s.t. (is_init S e ^ REQ_INITIALLY_ASSERTED) ? is_minus e : is_plus e { @e },
                en_req_withdraw = exists e in E REQS s.t. (is_init S e ^ REQ_INITIALLY_ASSERTED)  ? is_plus e : is_minus e { @e },
                en_req = en_req_assert | en_req_withdraw,
                // Acknowledgement assert/withdraw/change is enabled (correcting for the polarity and initial state of handshake).
                en_ack_assert = exists e in E ACKS s.t. (is_init S e ^ ACK_INITIALLY_ASSERTED) ? is_minus e : is_plus e { @e },
                en_ack_withdraw = exists e in E ACKS s.t. (is_init S e ^ ACK_INITIALLY_ASSERTED) ? is_plus e : is_minus e { @e },
                en_ack = en_ack_assert | en_ack_withdraw
            {
                // Check that the handshake progresses as expected, i.e. signals are not enabled unexpectedly.
                ~req & ~ack & (en_req_withdraw | en_ack)
                |
                 req & ~ack & (en_req | en_ack_withdraw)
                |
                 req &  ack & (en_req_assert | en_ack)
                |
                ~req &  ack & (en_req | en_ack_assert)
            }
            """.formatted(REQ_NAMES_REPLACEMENT, ACK_NAMES_REPLACEMENT,
            REQ_INITIALLY_ASSERTED_REPLACEMENT, ACK_INITIALLY_ASSERTED_REPLACEMENT);

    private static final String RECEPTIVENESS_HANDSHAKE_REACH = """
            // Handshake protocol receptiveness check
            let
                // Non-empty set of request signals.
                REQS = S {%s},
                // Non-empty set of acknowledgement signals.
                ACKS = S {%s},
                // If true then check assertion/withdrawal receptiveness.
                CHECK_ASSERT_ENABLED = %s,
                CHECK_WITHDRAW_ENABLED = %s,
                // The following two values specify the initial state of the handshake.
                REQ_INITIALLY_ASSERTED = %s,
                ACK_INITIALLY_ASSERTED = %s,

                // Handshake is active if the request is an output;
                // if there are several requests, they must all be of the same type.
                active = exists s in REQS { is_output s },

                // Some request is asserted (correcting for the polarity and initial state of handshake).
                req = exists s in REQS { $ s ^ (is_init s ^ REQ_INITIALLY_ASSERTED) },
                // Some acknowledgement is asserted (correcting for the polarity and initial state of handshake).
                ack = exists s in ACKS { $ s ^ (is_init s ^ ACK_INITIALLY_ASSERTED) },

                // Request assert/withdraw/change is enabled (correcting for the polarity and initial state of handshake).
                en_req_assert = exists e in E REQS s.t. (is_init S e ^ REQ_INITIALLY_ASSERTED) ? is_minus e : is_plus e { @e },
                en_req_withdraw = exists e in E REQS s.t. (is_init S e ^ REQ_INITIALLY_ASSERTED)  ? is_plus e : is_minus e { @e },
                // Acknowledgement assert/withdraw/change is enabled (correcting for the polarity and initial state of handshake).
                en_ack_assert = exists e in E ACKS s.t. (is_init S e ^ ACK_INITIALLY_ASSERTED) ? is_minus e : is_plus e { @e },
                en_ack_withdraw = exists e in E ACKS s.t. (is_init S e ^ ACK_INITIALLY_ASSERTED) ? is_plus e : is_minus e { @e }
            {
                // If the handshake is active, check that the appropriate acknowledgement edge
                // is enabled after request changes, unless this check is disabled.
                active & (CHECK_ASSERT_ENABLED & req & ~ack & ~en_ack_assert | CHECK_WITHDRAW_ENABLED & ~req & ack & ~en_ack_withdraw)
                |
                // If the handshake is passive, check that the appropriate request edge
                // is enabled after acknowledgement changes, unless this check is disabled.
                ~active & (CHECK_WITHDRAW_ENABLED & req & ack & ~en_req_withdraw | CHECK_ASSERT_ENABLED & ~req & ~ack & ~en_req_assert)
            }
            """.formatted(REQ_NAMES_REPLACEMENT, ACK_NAMES_REPLACEMENT,
            CHECK_ASSERT_ENABLED_REPLACEMENT, CHECK_WITHDRAW_ENABLED_REPLACEMENT,
            REQ_INITIALLY_ASSERTED_REPLACEMENT, ACK_INITIALLY_ASSERTED_REPLACEMENT);

    private final Set<String> reqs;
    private final Set<String> acks;
    private final boolean checkAssertion;
    private final boolean checkWithdrawal;
    private final State state;
    private final boolean allowInversion;

    public HandshakeParameters() {
        this(Collections.emptySet(), Collections.emptySet());
    }

    public HandshakeParameters(Collection<String> reqs, Collection<String> acks) {
        this(reqs, acks, true, true, State.REQ0ACK0, false);
    }

    public HandshakeParameters(Collection<String> reqs, Collection<String> acks,
            boolean checkAssertion, boolean checkWithdrawal, State state, boolean allowInversion) {

        this.reqs = new HashSet<>(reqs);
        this.acks = new HashSet<>(acks);
        this.checkAssertion = checkAssertion;
        this.checkWithdrawal = checkWithdrawal;
        this.state = state;
        this.allowInversion = allowInversion;
    }

    public Set<String> getReqs() {
        return Collections.unmodifiableSet(reqs);
    }

    public Set<String> getAcks() {
        return Collections.unmodifiableSet(acks);
    }

    public boolean isCheckAssertion() {
        return checkAssertion;
    }

    public boolean isCheckWithdrawal() {
        return checkWithdrawal;
    }

    public State getState() {
        return state;
    }

    public boolean isAllowInversion() {
        return allowInversion;
    }

    public List<VerificationParameters> getVerificationParametersList() {
        List<VerificationParameters> result = new ArrayList<>();
        if (!isAllowInversion()) {
            result.add(getInitialVerificationParameters());
        }
        result.add(getOnehotVerificationParameters());
        result.add(getOrderingVerificationParameters());
        result.add(getReceptivenessVerificationParameters());
        return result;
    }

    private VerificationParameters getInitialVerificationParameters() {
        String reach = INITIAL_HANDSHAKE_REACH
                .replace(REQ_NAMES_REPLACEMENT, getQuotedItemsAsString(getReqs()))
                .replace(ACK_NAMES_REPLACEMENT, getQuotedItemsAsString(getAcks()))
                .replace(REQ_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isReqAsserted()))
                .replace(ACK_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isAckAsserted()));

        return new VerificationParameters("Handshake initial state",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getPropertyCheckMessage(boolean propertyHolds) {
                return propertyHolds ? super.getPropertyCheckMessage(true) : VIOLATION_PREFIX
                        + "Wrong initial values of some requests or acknowledgements and inversions are forbidden.";
            }
        };
    }

    private VerificationParameters getOnehotVerificationParameters() {
        String reach = ONEHOT_HANDSHAKE_REACH
                .replace(REQ_NAMES_REPLACEMENT, getQuotedItemsAsString(getReqs()))
                .replace(ACK_NAMES_REPLACEMENT, getQuotedItemsAsString(getAcks()))
                .replace(REQ_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isReqAsserted()))
                .replace(ACK_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isAckAsserted()));

        return new VerificationParameters("Handshake one-hotness",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getPropertyCheckMessage(boolean propertyHolds) {
                return propertyHolds ? super.getPropertyCheckMessage(true) : VIOLATION_PREFIX
                        + "Requests and acknowledgements must be 1-hot (after correcting for polarity and initial state of handshake).";
            }
        };
    }

    private VerificationParameters getOrderingVerificationParameters() {
        String reach = ORDERING_HANDSHAKE_REACH
                .replace(REQ_NAMES_REPLACEMENT, getQuotedItemsAsString(getReqs()))
                .replace(ACK_NAMES_REPLACEMENT, getQuotedItemsAsString(getAcks()))
                .replace(REQ_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isReqAsserted()))
                .replace(ACK_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isAckAsserted()));

        return new VerificationParameters("Handshake ordering",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {

            @Override
            public String getPropertyCheckMessage(boolean propertyHolds) {
                return propertyHolds ? super.getPropertyCheckMessage(true) : VIOLATION_PREFIX
                        + "Wrong ordering of signal edges.";
            }
        };
    }
    private VerificationParameters getReceptivenessVerificationParameters() {
        String reach = RECEPTIVENESS_HANDSHAKE_REACH
                .replace(REQ_NAMES_REPLACEMENT, getQuotedItemsAsString(getReqs()))
                .replace(ACK_NAMES_REPLACEMENT, getQuotedItemsAsString(getAcks()))
                .replace(CHECK_ASSERT_ENABLED_REPLACEMENT, ReachUtils.getBooleanAsString(isCheckAssertion()))
                .replace(CHECK_WITHDRAW_ENABLED_REPLACEMENT, ReachUtils.getBooleanAsString(isCheckWithdrawal()))
                .replace(REQ_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isReqAsserted()))
                .replace(ACK_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isAckAsserted()));

        return new VerificationParameters("Handshake receptiveness",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true) {
            @Override
            public String getPropertyCheckMessage(boolean propertyHolds) {
                return propertyHolds ? super.getPropertyCheckMessage(true) : VIOLATION_PREFIX
                        + "Receptiveness does not hold.";
            }
        };
    }

    private String getQuotedItemsAsString(Collection<String> refs) {
        return refs.stream()
                .map(ref -> "\"" + ref + "\"")
                .collect(Collectors.joining(", "));
    }

}
