package org.workcraft.plugins.son.util;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;

public class Scenario extends MathNode {
    private String elements;

    public String getScenario() {
        return elements;
    }

    public void setScenario(String elements) {
        this.elements = NamespaceHelper.convertLegacyHierarchySeparators(elements);
    }

}
