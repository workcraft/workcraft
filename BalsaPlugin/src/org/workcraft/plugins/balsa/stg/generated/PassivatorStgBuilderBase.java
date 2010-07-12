package org.workcraft.plugins.balsa.stg.generated;

public abstract class PassivatorStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<PassivatorStgBuilderBase.Passivator, PassivatorStgBuilderBase.PassivatorHandshakes> {

    public static final class Passivator {

        public Passivator(org.workcraft.parsers.breeze.ParameterScope parameters) {
            count = (java.lang.Integer) parameters.get("count");
        }

        public final int count;
    }

    public static final class PassivatorHandshakesEnv {

        public PassivatorHandshakesEnv(Passivator component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.count, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> inp;
    }

    public static final class PassivatorHandshakes {

        public PassivatorHandshakes(Passivator component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.count, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> inp;
    }

    @Override
    public final Passivator makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Passivator(parameters);
    }

    @Override
    public final PassivatorHandshakes makeHandshakes(Passivator component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new PassivatorHandshakes(component, handshakes);
    }
}
