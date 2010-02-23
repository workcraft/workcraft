package org.workcraft.plugins.balsa.stg.generated;

public abstract class FetchStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<FetchStgBuilderBase.Fetch, FetchStgBuilderBase.FetchHandshakes> {

    public static final class Fetch {

        public Fetch(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            broad = (java.lang.Boolean) parameters.get("broad");
        }

        public final int width;

        public final boolean broad;
    }

    public static final class FetchHandshakes {

        public FetchHandshakes(Fetch component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            activate = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "activate", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync activate;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg out;
    }

    @Override
    public final Fetch makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Fetch(parameters);
    }

    @Override
    public final FetchHandshakes makeHandshakes(Fetch component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new FetchHandshakes(component, handshakes);
    }
}
