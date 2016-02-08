package org.workcraft.plugins.dfs.stg;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.generator.NodeStg;

public class RegisterStg extends NodeStg {
    public final VisualPlace M0;            // M=0
    public final VisualPlace M1;            // M=1
    public final VisualSignalTransition MR;    // M+
    public final VisualSignalTransition MF;    // M-

    public RegisterStg(
            VisualPlace M0, VisualPlace M1, VisualSignalTransition MR, VisualSignalTransition MF) {
        this.M0 = M0;
        this.M1 = M1;
        this.MR = MR;
        this.MF = MF;
    }

    @Override
    public List<VisualSignalTransition> getAllTransitions() {
        return Arrays.asList(MR, MF);
    }

    @Override
    public List<VisualPlace> getAllPlaces() {
        return Arrays.asList(M0, M1);
    }

}
