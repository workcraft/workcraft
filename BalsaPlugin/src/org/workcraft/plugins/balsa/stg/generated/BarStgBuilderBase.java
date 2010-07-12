package org.workcraft.plugins.balsa.stg.generated;

public abstract class BarStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<BarStgBuilderBase.Bar, BarStgBuilderBase.BarHandshakes> {

    public static final class Bar {

        public Bar(org.workcraft.parsers.breeze.ParameterScope parameters) {
            guardCount = (java.lang.Integer) parameters.get("guardCount");
        }

        public final int guardCount;
    }

    public static final class BarHandshakesEnv {

        public BarHandshakesEnv(Bar component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            guard = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "guard", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            guardInput = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "guardInput", component.guardCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.guardCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg guard;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> guardInput;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> activateOut;
    }

    public static final class BarHandshakes {

        public BarHandshakes(Bar component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            guard = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "guard", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            guardInput = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "guardInput", component.guardCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.guardCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg guard;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activate;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg> guardInput;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> activateOut;
    }

    @Override
    public final Bar makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Bar(parameters);
    }

    @Override
    public final BarHandshakes makeHandshakes(Bar component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new BarHandshakes(component, handshakes);
    }
}
