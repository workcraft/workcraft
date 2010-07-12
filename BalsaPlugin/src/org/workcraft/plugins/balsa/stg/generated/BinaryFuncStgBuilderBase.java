package org.workcraft.plugins.balsa.stg.generated;

public abstract class BinaryFuncStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<BinaryFuncStgBuilderBase.BinaryFunc, BinaryFuncStgBuilderBase.BinaryFuncHandshakes> {

    public static final class BinaryFunc {

        public BinaryFunc(org.workcraft.parsers.breeze.ParameterScope parameters) {
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

    public static final class BinaryFuncHandshakesEnv {

        public BinaryFuncHandshakesEnv(BinaryFunc component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inpB = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpB", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg inpA;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg inpB;
    }

    public static final class BinaryFuncHandshakes {

        public BinaryFuncHandshakes(BinaryFunc component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inpB = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpB", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inpA;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inpB;
    }

    @Override
    public final BinaryFunc makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new BinaryFunc(parameters);
    }

    @Override
    public final BinaryFuncHandshakes makeHandshakes(BinaryFunc component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new BinaryFuncHandshakes(component, handshakes);
    }
}
