package org.workcraft.plugins.balsa.stg.generated;

public abstract class SequenceOptimisedStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<SequenceOptimisedStgBuilderBase.SequenceOptimised, SequenceOptimisedStgBuilderBase.SequenceOptimisedHandshakes> {

    public static final class SequenceOptimised {

        public SequenceOptimised(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputCount = (java.lang.Integer) parameters.get("outputCount");
            specification = (java.lang.String) parameters.get("specification");
        }

        public final int outputCount;

        public final java.lang.String specification;
    }

    public static final class SequenceOptimisedHandshakesEnv {

        public SequenceOptimisedHandshakesEnv(SequenceOptimised component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> activateOut;
    }

    public static final class SequenceOptimisedHandshakes {

        public SequenceOptimisedHandshakes(SequenceOptimised component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> activateOut;
    }

    @Override
    public final SequenceOptimised makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new SequenceOptimised(parameters);
    }

    @Override
    public final SequenceOptimisedHandshakes makeHandshakes(SequenceOptimised component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new SequenceOptimisedHandshakes(component, handshakes);
    }
}
