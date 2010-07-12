package org.workcraft.plugins.balsa.stg.generated;

public abstract class SplitStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<SplitStgBuilderBase.Split, SplitStgBuilderBase.SplitHandshakes> {

    public static final class Split {

        public Split(org.workcraft.parsers.breeze.ParameterScope parameters) {
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
            LSOutputWidth = (java.lang.Integer) parameters.get("LSOutputWidth");
            MSOutputWidth = (java.lang.Integer) parameters.get("MSOutputWidth");
        }

        public final int inputWidth;

        public final int LSOutputWidth;

        public final int MSOutputWidth;
    }

    public static final class SplitHandshakesEnv {

        public SplitHandshakesEnv(Split component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            LSOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "LSOut", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            MSOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "MSOut", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg LSOut;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg MSOut;
    }

    public static final class SplitHandshakes {

        public SplitHandshakes(Split component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            LSOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "LSOut", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            MSOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "MSOut", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg LSOut;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg MSOut;
    }

    @Override
    public final Split makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Split(parameters);
    }

    @Override
    public final SplitHandshakes makeHandshakes(Split component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new SplitHandshakes(component, handshakes);
    }
}
