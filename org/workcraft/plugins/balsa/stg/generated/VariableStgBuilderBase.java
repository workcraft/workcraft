package org.workcraft.plugins.balsa.stg.generated;

public abstract class VariableStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<VariableStgBuilderBase.Variable, VariableStgBuilderBase.VariableHandshakes> {

    public static final class Variable {

        public Variable(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            readPortCount = (java.lang.Integer) parameters.get("readPortCount");
            name = (java.lang.String) parameters.get("name");
            specification = (java.lang.String) parameters.get("specification");
        }

        public final int width;

        public final int readPortCount;

        public final java.lang.String name;

        public final java.lang.String specification;
    }

    public static final class VariableHandshakes {

        public VariableHandshakes(Variable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            write = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "write", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            read = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "read", component.readPortCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg write;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> read;
    }

    @Override
    public final Variable makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Variable(parameters);
    }

    @Override
    public final VariableHandshakes makeHandshakes(Variable component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new VariableHandshakes(component, handshakes);
    }
}
