package org.workcraft.plugins.circuit.references;

import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.serialisation.References;

public class CircuitReferenceManager extends HierarchyReferenceManager {

    public CircuitReferenceManager(References refs) {
        super(refs);
    }

    @Override
    protected CircuitNameManager createNameManager() {
        return new CircuitNameManager();
    }

}
