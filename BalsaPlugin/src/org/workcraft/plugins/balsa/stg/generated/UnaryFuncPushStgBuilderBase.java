package org.workcraft.plugins.balsa.stg.generated;

public abstract class UnaryFuncPushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<UnaryFuncPushStgBuilderBase.UnaryFuncPush, UnaryFuncPushStgBuilderBase.UnaryFuncPushHandshakes> {

    public static final class UnaryFuncPush {

        public UnaryFuncPush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
            op = (org.workcraft.plugins.balsa.components.UnaryOperator) parameters.get("op");
            inputIsSigned = (java.lang.Boolean) parameters.get("inputIsSigned");
        }

        public final int outputWidth;

        public final int inputWidth;

        public final org.workcraft.plugins.balsa.components.UnaryOperator op;

        public final boolean inputIsSigned;
    }

    public static final class UnaryFuncPushHandshakesEnv {

        public UnaryFuncPushHandshakesEnv(UnaryFuncPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg inp;
    }

    public static final class UnaryFuncPushHandshakes {

        public UnaryFuncPushHandshakes(UnaryFuncPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;
    }

    @Override
    public final UnaryFuncPush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new UnaryFuncPush(parameters);
    }

    @Override
    public final UnaryFuncPushHandshakes makeHandshakes(UnaryFuncPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new UnaryFuncPushHandshakes(component, handshakes);
    }
}
