package org.workcraft.plugins.mpsat_verification.presets;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class HandshakeParameters {

    public enum Type {
        PASSIVE,
        ACTIVE;
    }

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

    private static final String ALLOW_INVERSIONS_REPLACEMENT =
            "/* insert inversion permission flag here */"; // true or false

    private static final String HANDSHAKE_REACH =
            "// Checks whether the given request(s) and acknowledgement(s) form a handshake;\n" +
            "// optionally, one can allow inverting some of these signals and specify the initial phase of the handshake.\n" +
            "let\n" +
            "    // Non-empty set of request signal names.\n" +
            "    REQ_NAMES = {" + REQ_NAMES_REPLACEMENT + "},\n" +
            "    // Non-empty set of acknowledgement signal names.\n" +
            "    ACK_NAMES = {" + ACK_NAMES_REPLACEMENT + "},\n" +
            "    // If true then check assertion/withdrawal receptiveness.\n" +
            "    CHECK_ASSERT_ENABLED = " + CHECK_ASSERT_ENABLED_REPLACEMENT + ",\n" +
            "    CHECK_WITHDRAW_ENABLED = " + CHECK_WITHDRAW_ENABLED_REPLACEMENT + ",\n" +
            "    // The following two values specify the initial state of the handshake.\n" +
            "    REQ_INITIALLY_ASSERTED = " + REQ_INITIALLY_ASSERTED_REPLACEMENT + ",\n" +
            "    ACK_INITIALLY_ASSERTED = " + ACK_INITIALLY_ASSERTED_REPLACEMENT + ",\n" +
            "    // If true then arbitrary inversions of signals are allowed.\n" +
            "    ALLOW_INVERSIONS = " + ALLOW_INVERSIONS_REPLACEMENT + ",\n" +
            "\n" +
            "    // Auxiliary calculated set of requests.\n" +
            "    reqs = gather nm in REQ_NAMES { S nm },\n" +
            "    // Auxiliary calculated set of acknowledgements.\n" +
            "    acks = gather nm in ACK_NAMES { S nm },\n" +
            "\n" +
            "    // Handshake is active if the request is an output;\n" +
            "    // if there are several requests, they must all be of the same type.\n" +
            "    active = exists s in reqs { is_output s },\n" +
            "    // Some request is asserted (correcting for the polarity and initial state of handshake).\n" +
            "    req = exists s in reqs { $s ^ (is_init s ^ REQ_INITIALLY_ASSERTED) },\n" +
            "    // Some acknowledgement is asserted (correcting for the polarity and initial state of handshake).\n" +
            "    ack = exists s in acks { $s ^ (is_init s ^ ACK_INITIALLY_ASSERTED) },\n" +
            "\n" +
            "    // Request assert/withdraw/change is enabled (correcting for the polarity and initial state of handshake).\n" +
            "    en_req_assert = exists e in ev reqs s.t. (is_init sig e ^ REQ_INITIALLY_ASSERTED) ? is_minus e : is_plus e { @e },\n" +
            "    en_req_withdraw = exists e in ev reqs s.t. (is_init sig e ^ REQ_INITIALLY_ASSERTED)  ? is_plus e : is_minus e { @e },\n" +
            "    en_req = en_req_assert | en_req_withdraw,\n" +
            "    // Acknowledgement assert/withdraw/change is enabled (correcting for the polarity and initial state of handshake).\n" +
            "    en_ack_assert = exists e in ev acks s.t. (is_init sig e ^ ACK_INITIALLY_ASSERTED) ? is_minus e : is_plus e { @e },\n" +
            "    en_ack_withdraw = exists e in ev acks s.t. (is_init sig e ^ ACK_INITIALLY_ASSERTED) ? is_plus e : is_minus e { @e },\n" +
            "    en_ack = en_ack_assert | en_ack_withdraw\n" +
            "{\n" +
            "    // Check that all requests are of the same type.\n" +
            "    exists s in reqs { ~(active ? is_output s : is_input s) } ?\n" +
            "    fail \"The requests must be of the same type (either inputs or outputs)\" :\n" +
            "    // Check that all acknowledgement have the type opposite to that of requests.\n" +
            "    exists s in acks { ~(active ? is_input s : is_output s) } ?\n" +
            "    fail \"The acknowledgements must be of the opposite type (either inputs or outputs) to requests\" :\n" +
            "\n" +
            "    // Check that either inversions are allowed or the initial values of requests are equal to REQ_INITIALLY_ASSERTED.\n" +
            "    ~ALLOW_INVERSIONS & exists s in reqs { is_init s ^ REQ_INITIALLY_ASSERTED } ?\n" +
            "    fail \"The initial values of some request(s) are wrong and inversions are not allowed\" :\n" +
            "    // Check that either inversions are allowed or the initial values of acknowledgements are equal to ACK_INITIALLY_ASSERTED.\n" +
            "    ~ALLOW_INVERSIONS & exists s in acks { is_init s ^ ACK_INITIALLY_ASSERTED } ?\n" +
            "    fail \"The initial values of some acknowledgement(s) are wrong and inversions are not allowed\" :\n" +
            "\n" +
            "    // Check that the requests are 1-hot (correcting for the polarity and initial state of handshake).\n" +
            "    threshold s in reqs { $s ^ (is_init s ^ REQ_INITIALLY_ASSERTED) }\n" +
            "    |\n" +
            "    // Check that the acknowledgement are 1-hot (correcting for the polarity and initial state of handshake).\n" +
            "    threshold s in acks { $s ^ (is_init s ^ ACK_INITIALLY_ASSERTED) }\n" +
            "    |\n" +
            "    // Check that the handshake progresses as expected, i.e. signals are not enabled unexpectedly.\n" +
            "    ~req & ~ack & (en_req_withdraw | en_ack)\n" +
            "    |\n" +
            "     req & ~ack & (en_req | en_ack_withdraw)\n" +
            "    |\n" +
            "     req &  ack & (en_req_assert | en_ack)\n" +
            "    |\n" +
            "    ~req &  ack & (en_req | en_ack_assert)\n" +
            "    |\n" +
            "    // If the handshake is active, check that the appropriate acknowledgement edge\n" +
            "    // is enabled after request changes, unless this check is disabled.\n" +
            "    active & (CHECK_ASSERT_ENABLED & req & ~ack & ~en_ack_assert | CHECK_WITHDRAW_ENABLED & ~req & ack & ~en_ack_withdraw)\n" +
            "    |\n" +
            "    // If the handshake is passive, check that the appropriate request edge\n" +
            "    // is enabled after acknowledgement changes, unless this check is disabled.\n" +
            "    ~active & (CHECK_WITHDRAW_ENABLED & req & ack & ~en_req_withdraw | CHECK_ASSERT_ENABLED & ~req & ~ack & ~en_req_assert)\n" +
            "}\n";

    private final Type type;
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
        this(Type.PASSIVE, reqs, acks, true, true, State.REQ0ACK0, false);
    }

    public HandshakeParameters(Type type, Collection<String> reqs, Collection<String> acks,
            boolean checkAssertion, boolean checkWithdrawal, State state, boolean allowInversion) {

        this.type = type;
        this.reqs = new HashSet<>(reqs);
        this.acks = new HashSet<>(acks);
        this.checkAssertion = checkAssertion;
        this.checkWithdrawal = checkWithdrawal;
        this.state = state;
        this.allowInversion = allowInversion;
    }

    public Type getType() {
        return type;
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

    public VerificationParameters getVerificationParameters() {

        String reach = HANDSHAKE_REACH
                .replace(REQ_NAMES_REPLACEMENT, getQuotedSelectedItems(getReqs()))
                .replace(ACK_NAMES_REPLACEMENT, getQuotedSelectedItems(getAcks()))
                .replace(CHECK_ASSERT_ENABLED_REPLACEMENT, ReachUtils.getBooleanAsString(isCheckAssertion()))
                .replace(CHECK_WITHDRAW_ENABLED_REPLACEMENT, ReachUtils.getBooleanAsString(isCheckWithdrawal()))
                .replace(REQ_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isReqAsserted()))
                .replace(ACK_INITIALLY_ASSERTED_REPLACEMENT, ReachUtils.getBooleanAsString(getState().isAckAsserted()))
                .replace(ALLOW_INVERSIONS_REPLACEMENT, ReachUtils.getBooleanAsString(isAllowInversion()));

        return new VerificationParameters("Handshake protocol",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

    private String getQuotedSelectedItems(Collection<String> list) {
        return list.stream()
                .map(ref -> "\"" + ref + "\"")
                .collect(Collectors.joining(", "));
    }

}
