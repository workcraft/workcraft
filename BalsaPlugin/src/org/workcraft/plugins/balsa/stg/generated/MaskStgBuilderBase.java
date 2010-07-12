package org.workcraft.plugins.balsa.stg.generated;

public abstract class MaskStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<MaskStgBuilderBase.Mask, MaskStgBuilderBase.MaskHandshakes> {

    public static final class Mask {

        public Mask(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
            mask = (java.lang.Integer) parameters.get("mask");
        }

        public final int outputWidth;

        public final int inputWidth;

        public final int mask;
    }

    public static final class MaskHandshakesEnv {

        public MaskHandshakesEnv(Mask component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg inp;
    }

    public static final class MaskHandshakes {

        public MaskHandshakes(Mask component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg inp;
    }

    @Override
    public final Mask makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Mask(parameters);
    }

    @Override
    public final MaskHandshakes makeHandshakes(Mask component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new MaskHandshakes(component, handshakes);
    }
}
