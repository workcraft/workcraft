package org.workcraft.plugins.balsa.stg.generated;

public abstract class BuiltinVariableStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<BuiltinVariableStgBuilderBase.BuiltinVariable, BuiltinVariableStgBuilderBase.BuiltinVariableHandshakes> {

    public static final class BuiltinVariable {

        public BuiltinVariable(org.workcraft.parsers.breeze.ParameterScope parameters) {
            readPortCount = (java.lang.Integer) parameters.get("readPortCount");
            name = (java.lang.String) parameters.get("name");
        }

        public final int readPortCount;

        public final java.lang.String name;
    }

    public static final class BuiltinVariableHandshakes {

        public BuiltinVariableHandshakes(BuiltinVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            write = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "write", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            read = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "read", component.readPortCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg write;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> read;
    }

    @Override
    public final BuiltinVariable makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new BuiltinVariable(parameters);
    }

    @Override
    public final BuiltinVariableHandshakes makeHandshakes(BuiltinVariable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new BuiltinVariableHandshakes(component, handshakes);
    }
}
