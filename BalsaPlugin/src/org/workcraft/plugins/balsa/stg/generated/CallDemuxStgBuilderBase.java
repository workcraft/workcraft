package org.workcraft.plugins.balsa.stg.generated;

public abstract class CallDemuxStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CallDemuxStgBuilderBase.CallDemux, CallDemuxStgBuilderBase.CallDemuxHandshakes> {

    public static final class CallDemux {

        public CallDemux(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int width;

        public final int outputCount;
    }

    public static final class CallDemuxHandshakesEnv {

        public CallDemuxHandshakesEnv(CallDemux component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg> out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg inp;
    }

    public static final class CallDemuxHandshakes {

        public CallDemuxHandshakes(CallDemux component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inp;
    }

    @Override
    public final CallDemux makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new CallDemux(parameters);
    }

    @Override
    public final CallDemuxHandshakes makeHandshakes(CallDemux component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CallDemuxHandshakes(component, handshakes);
    }
}
