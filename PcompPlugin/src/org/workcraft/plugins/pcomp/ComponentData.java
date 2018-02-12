package org.workcraft.plugins.pcomp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtils;

public class ComponentData {

    private final HashMap<String, String> placesSrc2Dst = new HashMap<>();
    private final HashMap<String, String> transitionsDst2Src = new HashMap<>();

    public ComponentData(Element placesElement, Element transitionsElelemnt) {
        for (Element element: XmlUtils.getChildElements("map", placesElement)) {
            for (Element src: XmlUtils.getChildElements("source", element)) {
                for (Element dst: XmlUtils.getChildElements("target", element)) {
                    placesSrc2Dst.put(src.getTextContent(), dst.getTextContent());
                }
            }
        }
        for (Element element: XmlUtils.getChildElements("map", transitionsElelemnt)) {
            for (Element src: XmlUtils.getChildElements("source", element)) {
                for (Element dst: XmlUtils.getChildElements("target", element)) {
                    transitionsDst2Src.put(src.getTextContent(), dst.getTextContent());
                }
            }
        }
    }

    public HashSet<String> getSrcPlaces(String dst) {
        HashSet<String> result = new HashSet<>();
        for (Entry<String, String> entry: placesSrc2Dst.entrySet()) {
            if (dst.equals(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public String getDstPlace(String src) {
        return placesSrc2Dst.get(src);
    }

    public HashSet<String> getSrcPlaces() {
        return new HashSet<>(placesSrc2Dst.keySet());
    }

    public HashSet<String> getDstPlaces() {
        return new HashSet<>(placesSrc2Dst.values());
    }

    public String getSrcTransition(String dst) {
        return transitionsDst2Src.get(dst);
    }

    public HashSet<String> getDstTransitions(String src) {
        HashSet<String> result = new HashSet<>();
        for (Entry<String, String> entry: transitionsDst2Src.entrySet()) {
            if (src.equals(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public HashSet<String> getStcTransitions() {
        return new HashSet<>(transitionsDst2Src.keySet());
    }

    public HashSet<String> getDstTransitions() {
        return new HashSet<>(transitionsDst2Src.values());
    }

}
