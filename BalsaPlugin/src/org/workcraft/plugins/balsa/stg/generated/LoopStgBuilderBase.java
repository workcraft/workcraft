package org.workcraft.plugins.balsa.stg.generated;

public abstract class LoopStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<LoopStgBuilderBase.Loop, LoopStgBuilderBase.LoopHandshakes> {

    public static final class Loop {

        public Loop(org.workcraft.parsers.breeze.ParameterScope parameters) {
        }
    }

    public static final class LoopHandshakesEnv {

        public LoopHandshakesEnv(Loop component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activateOut", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync activate;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activateOut;
    }

    public static final class LoopHandshakes {

        public LoopHandshakes(Loop component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activateOut", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activate;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync activateOut;
    }

    @Override
    public final Loop makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Loop(parameters);
    }

    @Override
    public final LoopHandshakes makeHandshakes(Loop component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new LoopHandshakes(component, handshakes);
    }
}
