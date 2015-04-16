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

    public void addRefPage(VisualPage refPage) {
        refPages.add(refPage);
    }

    public void removeRefPage(VisualPage removedPage) {
        refPages.remove(removedPage);
    }

    public HashMap<String, VisualVertex> getVertMap() {
        return vertMap;
    }

    public boolean vertMapContainsKey(String key) {
        return vertMap.containsKey(key);
    }

    public void updateNormalForm(String normalForm) {
        this.normalForm = normalForm;
    }

    public void updateVertMap(HashMap<String, VisualVertex> newVertMap){
        this.vertMap = newVertMap;
    }

    public String getNormalForm() {
        return normalForm;
    }

    public HashSet<VisualPage> getRefPages() {
        return refPages;
    }

}
