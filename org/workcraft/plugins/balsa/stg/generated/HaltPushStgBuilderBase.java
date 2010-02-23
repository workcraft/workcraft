package org.workcraft.plugins.balsa.stg.generated;

public abstract class HaltPushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<HaltPushStgBuilderBase.HaltPush, HaltPushStgBuilderBase.HaltPushHandshakes> {

    public static final class HaltPush {

        public HaltPush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
        }

        public final int width;
    }

    public static final class HaltPushHandshakes {

        public HaltPushHandshakes(HaltPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;
    }

    @Override
    public final HaltPush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new HaltPush(parameters);
    }

    @Override
    public final HaltPushHandshakes makeHandshakes(HaltPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new HaltPushHandshakes(component, handshakes);
    }
}
