package org.workcraft.plugins.balsa.stg.generated;

public abstract class EncodeStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<EncodeStgBuilderBase.Encode, EncodeStgBuilderBase.EncodeHandshakes> {

    public static final class Encode {

        public Encode(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            inputCount = (java.lang.Integer) parameters.get("inputCount");
            specification = (java.lang.String) parameters.get("specification");
        }

        public final int outputWidth;

        public final int inputCount;

        public final java.lang.String specification;
    }

    public static final class EncodeHandshakesEnv {

        public EncodeHandshakesEnv(Encode component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg out;
    }

    public static final class EncodeHandshakes {

        public EncodeHandshakes(Encode component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg out;
    }

    @Override
    public final Encode makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Encode(parameters);
    }

    @Override
    public final EncodeHandshakes makeHandshakes(Encode component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new EncodeHandshakes(component, handshakes);
    }
}
