package org.workcraft.plugins.dfs.stg;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.generator.NodeStg;

public class RegisterStg extends NodeStg {
    public final VisualPlace m0;            // M=0
    public final VisualPlace m1;            // M=1
    public final VisualSignalTransition mR;    // M+
    public final VisualSignalTransition mF;    // M-

    public RegisterStg(
            VisualPlace m0, VisualPlace m1, VisualSignalTransition mR, VisualSignalTransition mF) {
        this.m0 = m0;
        this.m1 = m1;
        this.mR = mR;
        this.mF = mF;
    }

    @Override
    public List<VisualSignalTransition> getAllTransitions() {
        return Arrays.asList(mR, mF);
    }

    @Override
    public List<VisualPlace> getAllPlaces() {
        return Arrays.asList(m0, m1);
    }

}
