package org.workcraft.plugins.balsa.stg.generated;

public abstract class SynchStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<SynchStgBuilderBase.Synch, SynchStgBuilderBase.SynchHandshakes> {

    public static final class Synch {

        public Synch(org.workcraft.parsers.breeze.ParameterScope parameters) {
            inputCount = (java.lang.Integer) parameters.get("inputCount");
        }

        public final int inputCount;
    }

    public static final class SynchHandshakes {

        public SynchHandshakes(Synch component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync out;
    }

    @Override
    public final Synch makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Synch(parameters);
    }

    @Override
    public final SynchHandshakes makeHandshakes(Synch component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new SynchHandshakes(component, handshakes);
    }
}
