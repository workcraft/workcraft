package org.workcraft.plugins.balsa.stg.generated;

public abstract class UnaryFuncStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<UnaryFuncStgBuilderBase.UnaryFunc, UnaryFuncStgBuilderBase.UnaryFuncHandshakes> {

    public static final class UnaryFunc {

        public UnaryFunc(org.workcraft.parsers.breeze.ParameterScope parameters) {
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

    public static final class UnaryFuncHandshakes {

        public UnaryFuncHandshakes(UnaryFunc component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inp;
    }

    @Override
    public final UnaryFunc makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new UnaryFunc(parameters);
    }

    @Override
    public final UnaryFuncHandshakes makeHandshakes(UnaryFunc component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new UnaryFuncHandshakes(component, handshakes);
    }
}
