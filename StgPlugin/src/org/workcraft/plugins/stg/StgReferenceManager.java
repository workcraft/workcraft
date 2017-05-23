package org.workcraft.plugins.stg;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.serialisation.References;
import org.workcraft.util.Pair;

public class StgReferenceManager extends HierarchicalUniqueNameReferenceManager {

    public StgReferenceManager(References refs) {
        super(refs);
    }

    @Override
    protected StgNameManager createNameManager() {
        return new StgNameManager() {
            @Override
            public String getPrefix(Node node) {
                return StgReferenceManager.this.getPrefix(node);
            }
        };
    }

    @Override
    protected void setExistingReference(Node node) {
        if ((node instanceof StgPlace) && ((StgPlace) node).isImplicit()) return;
        super.setExistingReference(node);
    }

    private StgNameManager getNameManager(Node node) {
        NamespaceProvider namespaceProvider = getNamespaceProvider(node);
        return (StgNameManager) getNameManager(namespaceProvider);
    }

    public Pair<String, Integer> getNamePair(Node node) {
        StgNameManager mgr = getNameManager(node);
        Pair<String, Integer> result = null;
        if (mgr.isNamed(node)) {
            result = mgr.getNamePair(node);
        }
        return result;
    }

    public int getInstanceNumber(Node node) {
        StgNameManager mgr = getNameManager(node);
        int result = 0;
        if (mgr.isNamed(node)) {
            result = mgr.getInstanceNumber(node);
        }
        return result;
    }

    public void setInstanceNumber(Node node, int number) {
        StgNameManager mgr = getNameManager(node);
        mgr.setInstanceNumber(node, number);
    }

    public void setDefaultNameIfUnnamed(Node node) {
        StgNameManager mgr = getNameManager(node);
        mgr.setDefaultNameIfUnnamed(node);
    }

    public void setName(Node node, String name, boolean forceInstance) {
        StgNameManager mgr = getNameManager(node);
        mgr.setName(node, name, forceInstance);
    }

    @Override
    public String getPrefix(Node node) {
        if (node instanceof StgPlace) return "p";
        if (node instanceof SignalTransition) {
            switch (((SignalTransition) node).getSignalType()) {
            case INPUT: return "in";
            case OUTPUT: return "out";
            case INTERNAL: return "sig";
            }
        }
        if (node instanceof DummyTransition) return "dum";
        return super.getPrefix(node);
    }

}
