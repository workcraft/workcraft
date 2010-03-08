package org.workcraft.plugins.balsa.stg.generated;

public abstract class PassivatorPushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<PassivatorPushStgBuilderBase.PassivatorPush, PassivatorPushStgBuilderBase.PassivatorPushHandshakes> {

    public static final class PassivatorPush {

        public PassivatorPush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int width;

        public final int outputCount;
    }

    public static final class PassivatorPushHandshakes {

        public PassivatorPushHandshakes(PassivatorPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "out", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;
    }

    @Override
    public final PassivatorPush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new PassivatorPush(parameters);
    }

    @Override
    public final PassivatorPushHandshakes makeHandshakes(PassivatorPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new PassivatorPushHandshakes(component, handshakes);
    }
}
