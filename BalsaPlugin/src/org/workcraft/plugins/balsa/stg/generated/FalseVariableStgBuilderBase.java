package org.workcraft.plugins.balsa.stg.generated;

public abstract class FalseVariableStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<FalseVariableStgBuilderBase.FalseVariable, FalseVariableStgBuilderBase.FalseVariableHandshakes> {

    public static final class FalseVariable {

        public FalseVariable(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            readPortCount = (java.lang.Integer) parameters.get("readPortCount");
            specification = (java.lang.String) parameters.get("specification");
        }

        public final int width;

        public final int readPortCount;

        public final java.lang.String specification;
    }

    public static final class FalseVariableHandshakesEnv {

        public FalseVariableHandshakesEnv(FalseVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            write = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "write", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            signal = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "signal", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            read = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "read", component.readPortCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg write;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync signal;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg> read;
    }

    public static final class FalseVariableHandshakes {

        public FalseVariableHandshakes(FalseVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            write = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "write", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            signal = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "signal", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            read = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "read", component.readPortCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg write;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync signal;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> read;
    }

    @Override
    public final FalseVariable makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new FalseVariable(parameters);
    }

    @Override
    public final FalseVariableHandshakes makeHandshakes(FalseVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new FalseVariableHandshakes(component, handshakes);
    }
}
