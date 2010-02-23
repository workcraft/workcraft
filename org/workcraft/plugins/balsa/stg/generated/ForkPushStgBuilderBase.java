package org.workcraft.plugins.balsa.stg.generated;

public abstract class ForkPushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ForkPushStgBuilderBase.ForkPush, ForkPushStgBuilderBase.ForkPushHandshakes> {

    public static final class ForkPush {

        public ForkPush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int width;

        public final int outputCount;
    }

    public static final class ForkPushHandshakes {

        public ForkPushHandshakes(ForkPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg> out;
    }

    @Override
    public final ForkPush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new ForkPush(parameters);
    }

    @Override
    public final ForkPushHandshakes makeHandshakes(ForkPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ForkPushHandshakes(component, handshakes);
    }
}
