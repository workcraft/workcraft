package org.workcraft.plugins.balsa.stg.generated;

public abstract class ContinuePushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ContinuePushStgBuilderBase.ContinuePush, ContinuePushStgBuilderBase.ContinuePushHandshakes> {

    public static final class ContinuePush {

        public ContinuePush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
        }

        public final int width;
    }

    public static final class ContinuePushHandshakes {

        public ContinuePushHandshakes(ContinuePush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;
    }

    @Override
    public final ContinuePush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new ContinuePush(parameters);
    }

    @Override
    public final ContinuePushHandshakes makeHandshakes(ContinuePush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ContinuePushHandshakes(component, handshakes);
    }
}
