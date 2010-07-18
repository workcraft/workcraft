package org.workcraft.plugins.balsa.stg.generated;

public abstract class SliceStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<SliceStgBuilderBase.Slice, SliceStgBuilderBase.SliceHandshakes> {

    public static final class Slice {

        public Slice(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
            lowIndex = (java.lang.Integer) parameters.get("lowIndex");
        }

        public final int outputWidth;

        public final int inputWidth;

        public final int lowIndex;
    }

    public static final class SliceHandshakes {

        public SliceHandshakes(Slice component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inp;
    }

    @Override
    public final Slice makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Slice(parameters);
    }

    @Override
    public final SliceHandshakes makeHandshakes(Slice component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new SliceHandshakes(component, handshakes);
    }
}
