package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.stg.Stg;

public class VerificationContext {

    private final Stg stg;
    private final CompositionData compositionData;
    private final VerificationParameters verificationParameters;


    public VerificationContext(Stg stg, CompositionData compositionData,
            VerificationParameters verificationParameters) {

        this.stg = stg;
        this.compositionData = compositionData;
        this.verificationParameters = verificationParameters;
    }

    public Stg getStg() {
        return stg;
    }

    public CompositionData getCompositionData() {
        return compositionData;
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }

}
