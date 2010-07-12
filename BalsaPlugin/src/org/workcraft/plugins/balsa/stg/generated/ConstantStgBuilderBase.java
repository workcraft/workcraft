package org.workcraft.plugins.balsa.stg.generated;

public abstract class ConstantStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ConstantStgBuilderBase.Constant, ConstantStgBuilderBase.ConstantHandshakes> {

    public static final class Constant {

        public Constant(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            value = (java.lang.Integer) parameters.get("value");
        }

        public final int width;

        public final int value;
    }

    public static final class ConstantHandshakesEnv {

        public ConstantHandshakesEnv(Constant component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg out;
    }

    public static final class ConstantHandshakes {

        public ConstantHandshakes(Constant component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;
    }

    @Override
    public final Constant makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Constant(parameters);
    }

    @Override
    public final ConstantHandshakes makeHandshakes(Constant component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ConstantHandshakes(component, handshakes);
    }
}
