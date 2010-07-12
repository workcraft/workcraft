package org.workcraft.plugins.balsa.stg.generated;

public abstract class ArbiterStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ArbiterStgBuilderBase.Arbiter, ArbiterStgBuilderBase.ArbiterHandshakes> {

    public static final class Arbiter {

        public Arbiter(org.workcraft.parsers.breeze.ParameterScope parameters) {
        }
    }

    public static final class ArbiterHandshakesEnv {

        public ArbiterHandshakesEnv(Arbiter component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            inpB = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpB", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            outA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "outA", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            outB = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "outB", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync inpA;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync inpB;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync outA;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync outB;
    }

    public static final class ArbiterHandshakes {

        public ArbiterHandshakes(Arbiter component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inpA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpA", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            inpB = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inpB", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            outA = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "outA", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            outB = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "outB", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync inpA;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync inpB;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync outA;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync outB;
    }

    @Override
    public final Arbiter makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Arbiter(parameters);
    }

    @Override
    public final ArbiterHandshakes makeHandshakes(Arbiter component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ArbiterHandshakes(component, handshakes);
    }
}
