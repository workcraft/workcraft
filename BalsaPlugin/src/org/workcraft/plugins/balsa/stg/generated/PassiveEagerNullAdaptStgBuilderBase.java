package org.workcraft.plugins.balsa.stg.generated;

public abstract class PassiveEagerNullAdaptStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<PassiveEagerNullAdaptStgBuilderBase.PassiveEagerNullAdapt, PassiveEagerNullAdaptStgBuilderBase.PassiveEagerNullAdaptHandshakes> {

    public static final class PassiveEagerNullAdapt {

        public PassiveEagerNullAdapt(org.workcraft.parsers.breeze.ParameterScope parameters) {
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
        }

        public final int inputWidth;
    }

    public static final class PassiveEagerNullAdaptHandshakesEnv {

        public PassiveEagerNullAdaptHandshakesEnv(PassiveEagerNullAdapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            trigger = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "trigger", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            signal = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "signal", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync trigger;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync signal;
    }

    public static final class PassiveEagerNullAdaptHandshakes {

        public PassiveEagerNullAdaptHandshakes(PassiveEagerNullAdapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            trigger = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "trigger", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            signal = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "signal", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync trigger;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync signal;
    }

    @Override
    public final PassiveEagerNullAdapt makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new PassiveEagerNullAdapt(parameters);
    }

    @Override
    public final PassiveEagerNullAdaptHandshakes makeHandshakes(PassiveEagerNullAdapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new PassiveEagerNullAdaptHandshakes(component, handshakes);
    }
}
