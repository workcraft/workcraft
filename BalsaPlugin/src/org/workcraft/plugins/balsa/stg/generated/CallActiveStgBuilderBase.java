package org.workcraft.plugins.balsa.stg.generated;

public abstract class CallActiveStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CallActiveStgBuilderBase.CallActive, CallActiveStgBuilderBase.CallActiveHandshakes> {

    public static final class CallActive {

        public CallActive(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int outputCount;
    }

    public static final class CallActiveHandshakesEnv {

        public CallActiveHandshakesEnv(CallActive component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> out;
    }

    public static final class CallActiveHandshakes {

        public CallActiveHandshakes(CallActive component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> out;
    }

    @Override
    public final CallActive makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new CallActive(parameters);
    }

    @Override
    public final CallActiveHandshakes makeHandshakes(CallActive component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CallActiveHandshakes(component, handshakes);
    }
}
