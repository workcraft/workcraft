package org.workcraft.plugins.balsa.stg.generated;

public abstract class HaltStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<HaltStgBuilderBase.Halt, HaltStgBuilderBase.HaltHandshakes> {

    public static final class Halt {

        public Halt(org.workcraft.parsers.breeze.ParameterScope parameters) {
        }
    }

    public static final class HaltHandshakes {

        public HaltHandshakes(Halt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync inp;
    }

    @Override
    public final Halt makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Halt(parameters);
    }

    @Override
    public final HaltHandshakes makeHandshakes(Halt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new HaltHandshakes(component, handshakes);
    }
}
