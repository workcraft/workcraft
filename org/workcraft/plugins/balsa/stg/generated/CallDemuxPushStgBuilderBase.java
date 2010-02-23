package org.workcraft.plugins.balsa.stg.generated;

public abstract class CallDemuxPushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CallDemuxPushStgBuilderBase.CallDemuxPush, CallDemuxPushStgBuilderBase.CallDemuxPushHandshakes> {

    public static final class CallDemuxPush {

        public CallDemuxPush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int width;

        public final int outputCount;
    }

    public static final class CallDemuxPushHandshakes {

        public CallDemuxPushHandshakes(CallDemuxPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg> out;
    }

    @Override
    public final CallDemuxPush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new CallDemuxPush(parameters);
    }

    @Override
    public final CallDemuxPushHandshakes makeHandshakes(CallDemuxPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CallDemuxPushHandshakes(component, handshakes);
    }
}
