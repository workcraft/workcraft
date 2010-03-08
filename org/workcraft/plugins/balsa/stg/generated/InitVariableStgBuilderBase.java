package org.workcraft.plugins.balsa.stg.generated;

public abstract class InitVariableStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<InitVariableStgBuilderBase.InitVariable, InitVariableStgBuilderBase.InitVariableHandshakes> {

    public static final class InitVariable {

        public InitVariable(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            readPortCount = (java.lang.Integer) parameters.get("readPortCount");
            initValue = (java.lang.Integer) parameters.get("initValue");
            name = (java.lang.String) parameters.get("name");
        }

        public final int width;

        public final int readPortCount;

        public final int initValue;

        public final java.lang.String name;
    }

    public static final class InitVariableHandshakes {

        public InitVariableHandshakes(InitVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            write = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "write", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            init = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "init", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
            read = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "read", component.readPortCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg write;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync init;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> read;
    }

    @Override
    public final InitVariable makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new InitVariable(parameters);
    }

    @Override
    public final InitVariableHandshakes makeHandshakes(InitVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new InitVariableHandshakes(component, handshakes);
    }
}
