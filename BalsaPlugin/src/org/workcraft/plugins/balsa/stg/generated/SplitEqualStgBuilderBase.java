package org.workcraft.plugins.balsa.stg.generated;

public abstract class SplitEqualStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<SplitEqualStgBuilderBase.SplitEqual, SplitEqualStgBuilderBase.SplitEqualHandshakes> {

    public static final class SplitEqual {

        public SplitEqual(org.workcraft.parsers.breeze.ParameterScope parameters) {
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int inputWidth;

        public final int outputWidth;

        public final int outputCount;
    }

    public static final class SplitEqualHandshakes {

        public SplitEqualHandshakes(SplitEqual component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg> out;
    }

    @Override
    public final SplitEqual makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new SplitEqual(parameters);
    }

    @Override
    public final SplitEqualHandshakes makeHandshakes(SplitEqual component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new SplitEqualHandshakes(component, handshakes);
    }
}
