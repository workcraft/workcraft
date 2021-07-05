package org.workcraft.plugins.stg.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.plugins.stg.NamedTransition;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.serialisation.References;

public class StgReferenceManager extends HierarchyReferenceManager {

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

    @Override
    public StgNameManager getNameManager(Node node) {
        return (StgNameManager) super.getNameManager(node);
    }

    public int getInstanceNumber(NamedTransition namedTransition) {
        StgNameManager mgr = getNameManager(namedTransition);
        int result = 0;
        if (mgr.isNamed(namedTransition)) {
            result = mgr.getInstanceNumber(namedTransition);
        }
        return result;
    }

    public void setInstanceNumber(NamedTransition namedTransition, int number) {
        StgNameManager mgr = getNameManager(namedTransition);
        mgr.setInstanceNumber(namedTransition, number);
    }

    public void setDefaultNameIfUnnamed(Node node) {
        StgNameManager mgr = getNameManager(node);
        mgr.setDefaultNameIfUnnamed(node);
    }

    @Override
    public void setName(Node node, String name, boolean force) {
        StgNameManager mgr = getNameManager(node);
        mgr.setName(node, name, force);
    }

}
