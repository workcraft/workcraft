package org.workcraft.plugins.balsa.stg.generated;

public abstract class CaseStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CaseStgBuilderBase.Case, CaseStgBuilderBase.CaseHandshakes> {

    public static final class Case {

        public Case(org.workcraft.parsers.breeze.ParameterScope parameters) {
            inputWidth = (java.lang.Integer) parameters.get("inputWidth");
            outputCount = (java.lang.Integer) parameters.get("outputCount");
            specification = (java.lang.String) parameters.get("specification");
        }

        public final int inputWidth;

        public final int outputCount;

        public final java.lang.String specification;
    }

    public static final class CaseHandshakesEnv {

        public CaseHandshakesEnv(Case component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync> activateOut;
    }

    public static final class CaseHandshakes {

        public CaseHandshakes(Case component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "inp", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg.class);
            activateOut = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "activateOut", component.outputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg inp;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync> activateOut;
    }

    @Override
    public final Case makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new Case(parameters);
    }

    @Override
    public final CaseHandshakes makeHandshakes(Case component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CaseHandshakes(component, handshakes);
    }
}
