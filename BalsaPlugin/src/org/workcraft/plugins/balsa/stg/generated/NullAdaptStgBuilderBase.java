package org.workcraft.plugins.balsa.stg.generated;

public abstract class NullAdaptStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<NullAdaptStgBuilderBase.NullAdapt, NullAdaptStgBuilderBase.NullAdaptHandshakes> {

    public static final class NullAdapt {

        public NullAdapt(org.workcraft.parsers.breeze.ParameterScope parameters) {
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
        }

        public final int inputWidth;
    }

    public static final class NullAdaptHandshakesEnv {

        public NullAdaptHandshakesEnv(NullAdapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg inp;
    }

    public static final class NullAdaptHandshakes {

        public NullAdaptHandshakes(NullAdapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;
    }

    @Override
    public final NullAdapt makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new NullAdapt(parameters);
    }

    @Override
    public final NullAdaptHandshakes makeHandshakes(NullAdapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new NullAdaptHandshakes(component, handshakes);
    }
}
