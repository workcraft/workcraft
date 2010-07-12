package org.workcraft.plugins.balsa.stg.generated;

public abstract class SynchPushStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<SynchPushStgBuilderBase.SynchPush, SynchPushStgBuilderBase.SynchPushHandshakes> {

    public static final class SynchPush {

        public SynchPush(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            outputCount = (java.lang.Integer) parameters.get("outputCount");
        }

        public final int width;

        public final int outputCount;
    }

    public static final class SynchPushHandshakesEnv {

        public SynchPushHandshakesEnv(SynchPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            pout = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "pout", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            aout = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "aout", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg> pout;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg aout;
    }

    public static final class SynchPushHandshakes {

        public SynchPushHandshakes(SynchPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            pout = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "pout", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            aout = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "aout", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> pout;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg aout;
    }

    @Override
    public final SynchPush makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new SynchPush(parameters);
    }

    @Override
    public final SynchPushHandshakes makeHandshakes(SynchPush component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new SynchPushHandshakes(component, handshakes);
    }
}
