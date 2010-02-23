package org.workcraft.plugins.balsa.stg.generated;

public abstract class ActiveEagerNullAdaptStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ActiveEagerNullAdaptStgBuilderBase.ActiveEagerNullAdapt, ActiveEagerNullAdaptStgBuilderBase.ActiveEagerNullAdaptHandshakes> {

    public static final class ActiveEagerNullAdapt {

        public ActiveEagerNullAdapt(org.workcraft.parsers.breeze.ParameterScope parameters) {
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
        }

        public final int inputWidth;
    }

    public static final class ActiveEagerNullAdaptHandshakes {

        public ActiveEagerNullAdaptHandshakes(ActiveEagerNullAdapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            trigger = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "trigger", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            signal = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "signal", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync trigger;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync signal;
    }

    @Override
    public final ActiveEagerNullAdapt makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new ActiveEagerNullAdapt(parameters);
    }

    @Override
    public final ActiveEagerNullAdaptHandshakes makeHandshakes(ActiveEagerNullAdapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ActiveEagerNullAdaptHandshakes(component, handshakes);
    }
}
