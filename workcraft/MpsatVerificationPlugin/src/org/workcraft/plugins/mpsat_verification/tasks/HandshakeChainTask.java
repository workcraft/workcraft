package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.HandshakeParameters;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HandshakeChainTask extends CombinedChainTask {

    private static final VerificationParameters TRIVIAL_VIOLATION_PARAMETERS = new VerificationParameters(
            "Handshake protocol", VerificationMode.UNDEFINED, 0,
            null, 0, null, false);

    private static final List<VerificationParameters> TRIVIAL_VIOLATION_PARAMETERS_LIST
            = Collections.singletonList(TRIVIAL_VIOLATION_PARAMETERS);

    private final WorkspaceEntry we;
    private final HandshakeParameters handshakeParameters;

    public HandshakeChainTask(WorkspaceEntry we, HandshakeParameters handshakeParameters) {
        super(we, handshakeParameters == null ? null : handshakeParameters.getVerificationParametersList());
        this.we = we;
        this.handshakeParameters = handshakeParameters;
    }

    @Override
    public Result<? extends CombinedChainOutput> run(ProgressMonitor<? super CombinedChainOutput> monitor) {
        Result<? extends CombinedChainOutput> result = super.run(monitor);
        return !result.isSuccess() ? result :
                Result.success(result.getPayload().applyMessage("Handshake protocol holds."));
    }

    @Override
    public Result<? extends CombinedChainOutput> checkTrivialCases() {
        Result<? extends CombinedChainOutput> result = super.checkTrivialCases();
        if (result != null) {
            return result;
        }
        // The model should be an STG
        if (!WorkspaceUtils.isApplicable(we, Stg.class)) {
            return Result.exception("Incorrect model type.");
        }

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Set<String> inputSignals = stg.getSignalReferences(Signal.Type.INPUT);
        Set<String> outputSignals = stg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> interfaceSignals = new HashSet<>(inputSignals);
        interfaceSignals.addAll(outputSignals);

        Set<String> reqs = handshakeParameters.getReqs();
        Set<String> acks = handshakeParameters.getAcks();

        if (!interfaceSignals.containsAll(reqs) && !interfaceSignals.containsAll(acks)) {
            return Result.exception("Only input/output signals are allowed.");
        }

        if (!outputSignals.containsAll(reqs) && !inputSignals.containsAll(reqs)) {
            return Result.success(new CombinedChainOutput()
                    .applyVerificationParametersList(TRIVIAL_VIOLATION_PARAMETERS_LIST)
                    .applyMessage(HandshakeParameters.VIOLATION_PREFIX
                            + "Requests must be of the same type (inputs or outputs)."));
        }

        if (outputSignals.containsAll(reqs) && !inputSignals.containsAll(acks)) {
            return Result.success(new CombinedChainOutput()
                    .applyVerificationParametersList(TRIVIAL_VIOLATION_PARAMETERS_LIST)
                    .applyMessage(HandshakeParameters.VIOLATION_PREFIX
                            + "Acknowledgements must be of opposite to requests type (inputs)."));
        }
        if (inputSignals.containsAll(reqs) && !outputSignals.containsAll(acks)) {
            return Result.success(new CombinedChainOutput()
                    .applyVerificationParametersList(TRIVIAL_VIOLATION_PARAMETERS_LIST)
                    .applyMessage(HandshakeParameters.VIOLATION_PREFIX
                            + "Acknowledgements must be of opposite to requests type (outputs)."));
        }

        return null;
    }

}
