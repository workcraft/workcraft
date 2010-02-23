package org.workcraft.plugins.balsa.stg.generated;

public abstract class CallStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CallStgBuilderBase.Call, CallStgBuilderBase.CallHandshakes> {

    public static final class Call {

        public Call(org.workcraft.parsers.breeze.ParameterScope parameters) {
            inputCount = (java.lang.Integer) parameters.get("inputCount");
        }

        public final int inputCount;
    }

    public static final class CallHandshakes {

        public CallHandshakes(Call component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync out;
    }

    @Override
    public final Call makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Call(parameters);
    }

    @Override
    public final CallHandshakes makeHandshakes(Call component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CallHandshakes(component, handshakes);
    }
}
