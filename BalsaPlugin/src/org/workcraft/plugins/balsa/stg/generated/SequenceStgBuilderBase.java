package org.workcraft.plugins.balsa.stg.generated;

public abstract class SequenceStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<SequenceStgBuilderBase.Sequence, SequenceStgBuilderBase.SequenceHandshakes> {

    public static final class Sequence {

        public Sequence(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int outputCount;
    }

    public static final class SequenceHandshakesEnv {

        public SequenceHandshakesEnv(Sequence component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> activateOut;
    }

    public static final class SequenceHandshakes {

        public SequenceHandshakes(Sequence component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> activateOut;
    }

    @Override
    public final Sequence makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Sequence(parameters);
    }

    @Override
    public final SequenceHandshakes makeHandshakes(Sequence component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new SequenceHandshakes(component, handshakes);
    }
}
