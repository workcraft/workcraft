package org.workcraft.plugins.balsa.stg.generated;

public abstract class BinaryFuncConstRStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<BinaryFuncConstRStgBuilderBase.BinaryFuncConstR, BinaryFuncConstRStgBuilderBase.BinaryFuncConstRHandshakes> {

    public static final class BinaryFuncConstR {

        public BinaryFuncConstR(org.workcraft.parsers.breeze.ParameterScope parameters) {
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

    public static final class BinaryFuncConstRHandshakesEnv {

        public BinaryFuncConstRHandshakesEnv(BinaryFuncConstR component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg inpA;
    }

    public static final class BinaryFuncConstRHandshakes {

        public BinaryFuncConstRHandshakes(BinaryFuncConstR component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inpA;
    }

    @Override
    public final BinaryFuncConstR makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new BinaryFuncConstR(parameters);
    }

    @Override
    public final BinaryFuncConstRHandshakes makeHandshakes(BinaryFuncConstR component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new BinaryFuncConstRHandshakes(component, handshakes);
    }
}
