package org.workcraft.plugins.balsa.stg.generated;

public abstract class AdaptStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<AdaptStgBuilderBase.Adapt, AdaptStgBuilderBase.AdaptHandshakes> {

    public static final class Adapt {

        public Adapt(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
            outputIsSigned = (java.lang.Boolean) parameters.get("outputIsSigned");
            inputIsSigned = (java.lang.Boolean) parameters.get("inputIsSigned");
        }

        public final int outputWidth;

        public final int inputWidth;

        public final boolean outputIsSigned;

        public final boolean inputIsSigned;
    }

    public static final class AdaptHandshakesEnv {

        public AdaptHandshakesEnv(Adapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg inp;
    }

    public static final class AdaptHandshakes {

        public AdaptHandshakes(Adapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inp;
    }

    @Override
    public final Adapt makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Adapt(parameters);
    }

    @Override
    public final AdaptHandshakes makeHandshakes(Adapt component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new AdaptHandshakes(component, handshakes);
    }
}
