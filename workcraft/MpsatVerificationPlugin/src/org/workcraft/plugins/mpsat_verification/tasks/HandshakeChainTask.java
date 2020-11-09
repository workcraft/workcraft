package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.HandshakeParameters;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.Result;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Set;

public class HandshakeChainTask extends CombinedChainTask {

    private static final String VIOLATION_PREFIX = "Handshake protocol is violated: ";

    private final WorkspaceEntry we;
    private final HandshakeParameters handshakeParameters;

    public HandshakeChainTask(WorkspaceEntry we, HandshakeParameters handshakeParameters) {
        super(we, handshakeParameters == null ? null : handshakeParameters.getVerificationParametersList());
        this.we = we;
        this.handshakeParameters = handshakeParameters;
    }

    @Override
    public Result<? extends CombinedChainOutput> checkTrivialCases() {
        Result<? extends CombinedChainOutput> result = super.checkTrivialCases();
        if (result != null) {
            return result;
        }
        // The model should be an STG
        if (!WorkspaceUtils.isApplicable(we, Stg.class)) {
            return Result.exception("Incorrect model type");
        }

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Set<String> inputSignals = stg.getSignalReferences(Signal.Type.INPUT);
        Set<String> outputSignals = stg.getSignalReferences(Signal.Type.OUTPUT);

        Set<String> reqs = handshakeParameters.getReqs();
        if (!outputSignals.containsAll(reqs) && !inputSignals.containsAll(reqs)) {
            return Result.exception(VIOLATION_PREFIX +
                    "Requests must be of the same type (inputs or outputs)");
        }

        Set<String> acks = handshakeParameters.getAcks();
        if (outputSignals.containsAll(reqs) && !inputSignals.containsAll(acks)) {
            return Result.exception(VIOLATION_PREFIX
                    + "Acknowledgements must be of opposite type (inputs) to requests");
        }
        if (inputSignals.containsAll(reqs) && !outputSignals.containsAll(acks)) {
            return Result.exception(VIOLATION_PREFIX
                    + "Acknowledgements must be of opposite type (outputs) to requests");
        }

        return null;
    }

}
