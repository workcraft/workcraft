package org.workcraft.plugins.cpog.tools;

import org.workcraft.dom.visual.VisualPage;
import org.workcraft.plugins.cpog.VisualVertex;

import java.util.HashMap;
import java.util.HashSet;

public class GraphReference {
    private String label, normalForm;
    private HashMap<String, VisualVertex> vertMap = new HashMap<String, VisualVertex>();
    private HashSet<VisualPage> refPages = new HashSet<VisualPage>();

    public GraphReference(String label, String normalForm, HashMap<String, VisualVertex> vertMap) {
        this.label = label;
        this.normalForm = normalForm;
        this.vertMap = vertMap;
    }
}
