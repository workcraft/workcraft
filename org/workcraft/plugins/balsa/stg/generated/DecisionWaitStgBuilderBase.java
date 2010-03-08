package org.workcraft.plugins.balsa.stg.generated;

public abstract class DecisionWaitStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<DecisionWaitStgBuilderBase.DecisionWait, DecisionWaitStgBuilderBase.DecisionWaitHandshakes> {

    public static final class DecisionWait {

        public DecisionWait(org.workcraft.parsers.breeze.ParameterScope parameters) {
            portCount = (java.lang.Integer) parameters.get("portCount");
        }

        public final int portCount;
    }

    public static final class DecisionWaitHandshakes {

        public DecisionWaitHandshakes(DecisionWait component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.portCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.portCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> out;
    }

    @Override
    public final DecisionWait makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new DecisionWait(parameters);
    }

    @Override
    public final DecisionWaitHandshakes makeHandshakes(DecisionWait component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new DecisionWaitHandshakes(component, handshakes);
    }
}
