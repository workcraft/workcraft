package org.workcraft.plugins.balsa.stg.generated;

public abstract class ConcurStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ConcurStgBuilderBase.Concur, ConcurStgBuilderBase.ConcurHandshakes> {

    public static final class Concur {

        public Concur(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int outputCount;
    }

    public static final class ConcurHandshakesEnv {

        public ConcurHandshakesEnv(Concur component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> activateOut;
    }

    public static final class ConcurHandshakes {

        public ConcurHandshakes(Concur component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> activateOut;
    }

    @Override
    public final Concur makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Concur(parameters);
    }

    @Override
    public final ConcurHandshakes makeHandshakes(Concur component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ConcurHandshakes(component, handshakes);
    }
}
