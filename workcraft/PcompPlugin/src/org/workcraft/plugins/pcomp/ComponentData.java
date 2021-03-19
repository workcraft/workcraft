package org.workcraft.plugins.pcomp;

import org.w3c.dom.Element;
import org.workcraft.utils.XmlUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ComponentData {

    private static final String MAP_ELEMENT_NAME = "map";
    private static final String SRC_ELEMENT_NAME = "src";
    private static final String DST_ELEMENT_NAME = "dst";

    private final String fileName;
    private final Map<String, String> src2dstPlaceMap = new HashMap<>();
    private final Map<String, String> dst2srcTransitionMap = new HashMap<>();

    public ComponentData(Element fileElement, Element placesElement, Element transitionsElement) {
        fileName = fileElement.getTextContent();
        for (Element element : XmlUtils.getChildElements(MAP_ELEMENT_NAME, placesElement)) {
            for (Element src : XmlUtils.getChildElements(SRC_ELEMENT_NAME, element)) {
                for (Element dst : XmlUtils.getChildElements(DST_ELEMENT_NAME, element)) {
                    src2dstPlaceMap.put(src.getTextContent(), dst.getTextContent());
                }
            }
        }
        for (Element element : XmlUtils.getChildElements(MAP_ELEMENT_NAME, transitionsElement)) {
            for (Element src : XmlUtils.getChildElements(SRC_ELEMENT_NAME, element)) {
                for (Element dst : XmlUtils.getChildElements(DST_ELEMENT_NAME, element)) {
                    dst2srcTransitionMap.put(dst.getTextContent(), src.getTextContent());
                }
            }
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getDstPlace(String src) {
        return src2dstPlaceMap.get(src);
    }

    public HashSet<String> getSrcPlaces() {
        return new HashSet<>(src2dstPlaceMap.keySet());
    }

    public Set<String> getDstPlaces() {
        return new HashSet<>(src2dstPlaceMap.values());
    }

    public String getSrcTransition(String dst) {
        return dst2srcTransitionMap.get(dst);
    }

    public Set<String> getDstTransitions(String src) {
        Set<String> result = new HashSet<>();
        for (Entry<String, String> entry: dst2srcTransitionMap.entrySet()) {
            if (src.equals(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Set<String> getSrcTransitions() {
        return new HashSet<>(dst2srcTransitionMap.values());
    }

    public Set<String> getDstTransitions() {
        return new HashSet<>(dst2srcTransitionMap.keySet());
    }

    public void addShadowTransition(String shadow, String src) {
        dst2srcTransitionMap.put(shadow, src);
    }

    public void substituteSrcTransitions(Map<String, String> substitutions) {
        for (Entry<String, String> entry : dst2srcTransitionMap.entrySet()) {
            String key = entry.getValue();
            String value = substitutions.get(key);
            if (value != null) {
                entry.setValue(value);
            }
        }
    }

}
