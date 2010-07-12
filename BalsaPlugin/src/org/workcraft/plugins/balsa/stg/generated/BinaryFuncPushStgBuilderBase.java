package org.workcraft.plugins.balsa.stg.generated;

public abstract class BinaryFuncPushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<BinaryFuncPushStgBuilderBase.BinaryFuncPush, BinaryFuncPushStgBuilderBase.BinaryFuncPushHandshakes> {

    public static final class BinaryFuncPush {

        public BinaryFuncPush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            inputAWidth = (java.lang.Integer) parameters.get("inputAWidth");
            inputBWidth = (java.lang.Integer) parameters.get("inputBWidth");
            op = (org.workcraft.plugins.balsa.components.BinaryOperator) parameters.get("op");
            outputIsSigned = (java.lang.Boolean) parameters.get("outputIsSigned");
            inputAIsSigned = (java.lang.Boolean) parameters.get("inputAIsSigned");
            inputBIsSigned = (java.lang.Boolean) parameters.get("inputBIsSigned");
        }

        public final int outputWidth;

        public final int inputAWidth;

        public final int inputBWidth;

        public final org.workcraft.plugins.balsa.components.BinaryOperator op;

        public final boolean outputIsSigned;

        public final boolean inputAIsSigned;

        public final boolean inputBIsSigned;
    }

    public static final class BinaryFuncPushHandshakesEnv {

        public BinaryFuncPushHandshakesEnv(BinaryFuncPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            inpB = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpB", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg inpA;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg inpB;
    }

    public static final class BinaryFuncPushHandshakes {

        public BinaryFuncPushHandshakes(BinaryFuncPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            inpB = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpB", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inpA;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inpB;
    }

    @Override
    public final BinaryFuncPush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new BinaryFuncPush(parameters);
    }

    @Override
    public final BinaryFuncPushHandshakes makeHandshakes(BinaryFuncPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new BinaryFuncPushHandshakes(component, handshakes);
    }
}
