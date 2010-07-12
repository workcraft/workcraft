package org.workcraft.plugins.balsa.stg.generated;

public abstract class PassiveSyncEagerFalseVariableStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<PassiveSyncEagerFalseVariableStgBuilderBase.PassiveSyncEagerFalseVariable, PassiveSyncEagerFalseVariableStgBuilderBase.PassiveSyncEagerFalseVariableHandshakes> {

    public static final class PassiveSyncEagerFalseVariable {

        public PassiveSyncEagerFalseVariable(org.workcraft.parsers.breeze.ParameterScope parameters) {
        }
    }

    public static final class PassiveSyncEagerFalseVariableHandshakesEnv {

        public PassiveSyncEagerFalseVariableHandshakesEnv(PassiveSyncEagerFalseVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            trigger = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "trigger", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
            signal = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "signal", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync trigger;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync signal;
    }

    public static final class PassiveSyncEagerFalseVariableHandshakes {

        public PassiveSyncEagerFalseVariableHandshakes(PassiveSyncEagerFalseVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            trigger = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "trigger", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            signal = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "signal", org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync trigger;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync inp;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync signal;
    }

    @Override
    public final PassiveSyncEagerFalseVariable makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new PassiveSyncEagerFalseVariable(parameters);
    }

    @Override
    public final PassiveSyncEagerFalseVariableHandshakes makeHandshakes(PassiveSyncEagerFalseVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new PassiveSyncEagerFalseVariableHandshakes(component, handshakes);
    }
}
