package org.workcraft.plugins.stg.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.UniqueReferenceManager;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.serialisation.References;

public class StgReferenceManager extends UniqueReferenceManager {

    public StgReferenceManager(References refs) {
        super(refs);
    }

    @Override
    protected StgNameManager createNameManager() {
        return new StgNameManager();
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

}
