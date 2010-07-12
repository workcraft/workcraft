package org.workcraft.plugins.balsa.stg.generated;

public abstract class WireForkStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<WireForkStgBuilderBase.WireFork, WireForkStgBuilderBase.WireForkHandshakes> {

    public static final class WireFork {

        public WireFork(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int outputCount;
    }

    public static final class WireForkHandshakesEnv {

        public WireForkHandshakesEnv(WireFork component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> out;
    }

    public static final class WireForkHandshakes {

        public WireForkHandshakes(WireFork component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> out;
    }

    @Override
    public final WireFork makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new WireFork(parameters);
    }

    @Override
    public final WireForkHandshakes makeHandshakes(WireFork component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new WireForkHandshakes(component, handshakes);
    }
}
