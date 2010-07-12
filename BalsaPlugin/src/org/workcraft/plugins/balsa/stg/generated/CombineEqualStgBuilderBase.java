package org.workcraft.plugins.balsa.stg.generated;

public abstract class CombineEqualStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CombineEqualStgBuilderBase.CombineEqual, CombineEqualStgBuilderBase.CombineEqualHandshakes> {

    public static final class CombineEqual {

        public CombineEqual(org.workcraft.parsers.breeze.ParameterScope parameters) {
            outputWidth = (java.lang.Integer) parameters.get("outputWidth");
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
            inputCount = (java.lang.Integer) parameters.get("inputCount");
        }

        public final int outputWidth;

        public final int inputWidth;

        public final int inputCount;
    }

    public static final class CombineEqualHandshakesEnv {

        public CombineEqualHandshakesEnv(CombineEqual component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg out;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> inp;
    }

    public static final class CombineEqualHandshakes {

        public CombineEqualHandshakes(CombineEqual component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg> inp;
    }

    @Override
    public final CombineEqual makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new CombineEqual(parameters);
    }

    @Override
    public final CombineEqualHandshakes makeHandshakes(CombineEqual component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CombineEqualHandshakes(component, handshakes);
    }
}
