package org.workcraft.plugins.balsa.stg.generated;

public abstract class ForkStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ForkStgBuilderBase.Fork, ForkStgBuilderBase.ForkHandshakes> {

    public static final class Fork {

        public Fork(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int outputCount;
    }

    public static final class ForkHandshakes {

        public ForkHandshakes(Fork component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> out;
    }

    @Override
    public final Fork makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Fork(parameters);
    }

    @Override
    public final ForkHandshakes makeHandshakes(Fork component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ForkHandshakes(component, handshakes);
    }
}
