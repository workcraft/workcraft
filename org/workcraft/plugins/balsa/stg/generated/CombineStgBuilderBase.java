package org.workcraft.plugins.balsa.stg.generated;

public abstract class CombineStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CombineStgBuilderBase.Combine, CombineStgBuilderBase.CombineHandshakes> {

    public static final class Combine {

        public Combine(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            LSInputWidth = (java.lang.Integer) parameters.get("LSInputWidth");
            MSInputWidth = (java.lang.Integer) parameters.get("MSInputWidth");
        }

        public final int outputWidth;

        public final int LSInputWidth;

        public final int MSInputWidth;
    }

    public static final class CombineHandshakes {

        public CombineHandshakes(Combine component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            LSInp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "LSInp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            MSInp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "MSInp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg LSInp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg MSInp;
    }

    @Override
    public final Combine makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Combine(parameters);
    }

    @Override
    public final CombineHandshakes makeHandshakes(Combine component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CombineHandshakes(component, handshakes);
    }
}
