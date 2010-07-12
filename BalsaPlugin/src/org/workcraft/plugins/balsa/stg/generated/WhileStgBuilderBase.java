package org.workcraft.plugins.balsa.stg.generated;

public abstract class WhileStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<WhileStgBuilderBase.While, WhileStgBuilderBase.WhileHandshakes> {

    public static final class While {

        public While(org.workcraft.parsers.breeze.ParameterScope parameters) {
        }
    }

    public static final class WhileHandshakesEnv {

        public WhileHandshakesEnv(While component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            guard = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "guard", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveFullDataPullStg.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activateOut", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync activate;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveFullDataPullStg guard;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activateOut;
    }

    public static final class WhileHandshakes {

        public WhileHandshakes(While component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            guard = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "guard", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveFullDataPullStg.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activateOut", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activate;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveFullDataPullStg guard;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync activateOut;
    }

    @Override
    public final While makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new While(parameters);
    }

    @Override
    public final WhileHandshakes makeHandshakes(While component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new WhileHandshakes(component, handshakes);
    }
}
