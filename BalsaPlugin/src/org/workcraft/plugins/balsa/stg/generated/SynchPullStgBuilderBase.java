package org.workcraft.plugins.balsa.stg.generated;

public abstract class SynchPullStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<SynchPullStgBuilderBase.SynchPull, SynchPullStgBuilderBase.SynchPullHandshakes> {

    public static final class SynchPull {

        public SynchPull(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int width;

        public final int outputCount;
    }

    public static final class SynchPullHandshakesEnv {

        public SynchPullHandshakesEnv(SynchPull component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            pout = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "pout", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg> pout;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg inp;
    }

    public static final class SynchPullHandshakes {

        public SynchPullHandshakes(SynchPull component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            pout = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "pout", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> pout;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inp;
    }

    @Override
    public final SynchPull makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new SynchPull(parameters);
    }

    @Override
    public final SynchPullHandshakes makeHandshakes(SynchPull component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new SynchPullHandshakes(component, handshakes);
    }
}
