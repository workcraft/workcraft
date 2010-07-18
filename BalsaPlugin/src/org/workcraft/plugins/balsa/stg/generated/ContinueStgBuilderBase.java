package org.workcraft.plugins.balsa.stg.generated;

public abstract class ContinueStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<ContinueStgBuilderBase.Continue, ContinueStgBuilderBase.ContinueHandshakes> {

    public static final class Continue {

        public Continue(org.workcraft.parsers.breeze.ParameterScope parameters) {
        }
    }

    public static final class ContinueHandshakes {

        public ContinueHandshakes(Continue component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync inp;
    }

    @Override
    public final Continue makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Continue(parameters);
    }

    @Override
    public final ContinueHandshakes makeHandshakes(Continue component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new ContinueHandshakes(component, handshakes);
    }
}
