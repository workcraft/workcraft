package org.workcraft.plugins.balsa.stg.generated;

public abstract class CallMuxStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CallMuxStgBuilderBase.CallMux, CallMuxStgBuilderBase.CallMuxHandshakes> {

    public static final class CallMux {

        public CallMux(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            inputCount = (java.lang.Integer) parameters.get("inputCount");
        }

        public final int width;

        public final int inputCount;
    }

    public static final class CallMuxHandshakes {

        public CallMuxHandshakes(CallMux component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg> inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg out;
    }

    @Override
    public final CallMux makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new CallMux(parameters);
    }

    @Override
    public final CallMuxHandshakes makeHandshakes(CallMux component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CallMuxHandshakes(component, handshakes);
    }
}
