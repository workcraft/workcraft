package org.workcraft.plugins.balsa.stg.generated;

public abstract class CaseFetchStgBuilderBase extends org.workcraft.plugins.balsa.stg.GeneratedComponentStgBuilder<CaseFetchStgBuilderBase.CaseFetch, CaseFetchStgBuilderBase.CaseFetchHandshakes> {

    public static final class CaseFetch {

        public CaseFetch(org.workcraft.parsers.breeze.ParameterScope parameters) {
            width = (java.lang.Integer) parameters.get("width");
            indexWidth = (java.lang.Integer) parameters.get("indexWidth");
            inputCount = (java.lang.Integer) parameters.get("inputCount");
            specification = (java.lang.String) parameters.get("specification");
        }

        public final int width;

        public final int indexWidth;

        public final int inputCount;

        public final java.lang.String specification;
    }

    public static final class CaseFetchHandshakesEnv {

        public CaseFetchHandshakesEnv(CaseFetch component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            index = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "index", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg index;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg> inp;
    }

    public static final class CaseFetchHandshakes {

        public CaseFetchHandshakes(CaseFetch component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
            out = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "out", org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg.class);
            index = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.get(handshakes, "index", org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
            inp = org.workcraft.plugins.balsa.stg.StgHandshakeInterpreter.array(handshakes, "inp", component.inputCount, org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg.class);
        }

        public final org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg out;

        public final org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg index;

        public final java.util.List<org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg> inp;
    }

    @Override
    public final CaseFetch makeProperties(org.workcraft.parsers.breeze.ParameterScope parameters) {
        return new CaseFetch(parameters);
    }

    @Override
    public final CaseFetchHandshakes makeHandshakes(CaseFetch component, java.util.Map<java.lang.String, org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface> handshakes) {
        return new CaseFetchHandshakes(component, handshakes);
    }
}
