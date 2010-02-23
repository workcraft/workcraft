package org.workcraft.plugins.balsa.stg.generated;

public abstract class BinaryFuncConstRPushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<BinaryFuncConstRPushStgBuilderBase.BinaryFuncConstRPush, BinaryFuncConstRPushStgBuilderBase.BinaryFuncConstRPushHandshakes> {

    public static final class BinaryFuncConstRPush {

        public BinaryFuncConstRPush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            inputAWidth = (java.lang.Integer) parameters.get("inputAWidth");
            inputBWidth = (java.lang.Integer) parameters.get("inputBWidth");
            op = (org.workcraft.plugins.balsa.components.BinaryOperator) parameters.get("op");
            outputIsSigned = (java.lang.Boolean) parameters.get("outputIsSigned");
            inputAIsSigned = (java.lang.Boolean) parameters.get("inputAIsSigned");
            inputBIsSigned = (java.lang.Boolean) parameters.get("inputBIsSigned");
            inputBValue = (java.lang.Integer) parameters.get("inputBValue");
        }

        public final int outputWidth;

        public final int inputAWidth;

        public final int inputBWidth;

        public final org.workcraft.plugins.balsa.components.BinaryOperator op;

        public final boolean outputIsSigned;

        public final boolean inputAIsSigned;

        public final boolean inputBIsSigned;

        public final int inputBValue;
    }

    public static final class BinaryFuncConstRPushHandshakes {

        public BinaryFuncConstRPushHandshakes(BinaryFuncConstRPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inpA;
    }

    @Override
    public final BinaryFuncConstRPush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new BinaryFuncConstRPush(parameters);
    }

    @Override
    public final BinaryFuncConstRPushHandshakes makeHandshakes(BinaryFuncConstRPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new BinaryFuncConstRPushHandshakes(component, handshakes);
    }
}
