package org.workcraft.plugins.pcomp;

import org.w3c.dom.Element;
import org.workcraft.plugins.stg.utils.ToggleUtils;
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
    private static final String TYPE_ATTRIBUTE_NAME = "type";

    private static final String INPUT_TYPE_ATTRIBUTE_VALUE = "input";
    private static final String OUTPUT_TYPE_ATTRIBUTE_VALUE = "output";
    private static final String INTERNAL_TYPE_ATTRIBUTE_VALUE = "internal";

    private final String fileName;
    private final Map<String, String> src2dstPlaceMap = new HashMap<>();
    private final Map<String, String> dst2srcTransitionMap = new HashMap<>();

    public ComponentData(Element fileElement, Element signalsElement, Element placesElement, Element transitionsElement) {
        fileName = fileElement.getTextContent();
        // Track src and dst signals to distinguish them from dummies and correct toggle transitions
        Set<String> srcSignals = new HashSet<>();
        Set<String> dstSignals = new HashSet<>();
        for (Element signalElement : XmlUtils.getChildElements(MAP_ELEMENT_NAME, signalsElement)) {
            for (Element srcElement : XmlUtils.getChildElements(SRC_ELEMENT_NAME, signalElement)) {
                if (isSignalAttribute(srcElement.getAttribute(TYPE_ATTRIBUTE_NAME))) {
                    srcSignals.add(srcElement.getTextContent());
                }
            }
            for (Element dstElement : XmlUtils.getChildElements(DST_ELEMENT_NAME, signalElement)) {
                if (isSignalAttribute(dstElement.getAttribute(TYPE_ATTRIBUTE_NAME))) {
                    dstSignals.add(dstElement.getTextContent());
                }
            }
        }
        // Track mapping of each src place to one or many dst places
        for (Element placeElement : XmlUtils.getChildElements(MAP_ELEMENT_NAME, placesElement)) {
            for (Element srcElement : XmlUtils.getChildElements(SRC_ELEMENT_NAME, placeElement)) {
                String srcPlaceRef = ToggleUtils.toggleIfImplicitPlace(srcElement.getTextContent(), srcSignals);
                for (Element dstElement : XmlUtils.getChildElements(DST_ELEMENT_NAME, placeElement)) {
                    String dstPlaceRef = ToggleUtils.toggleIfImplicitPlace(dstElement.getTextContent(), dstSignals);
                    src2dstPlaceMap.put(srcPlaceRef, dstPlaceRef);
                }
            }
        }
        // Track mapping of each dst transition to one or many src transitions
        for (Element transitionElement : XmlUtils.getChildElements(MAP_ELEMENT_NAME, transitionsElement)) {
            for (Element srcElement : XmlUtils.getChildElements(SRC_ELEMENT_NAME, transitionElement)) {
                String srcTransitionRef = ToggleUtils.toggleIfSignalTransition(srcElement.getTextContent(), srcSignals);
                for (Element dstElement : XmlUtils.getChildElements(DST_ELEMENT_NAME, transitionElement)) {
                    String dstTransitionRef = ToggleUtils.toggleIfSignalTransition(dstElement.getTextContent(), dstSignals);
                    dst2srcTransitionMap.put(dstTransitionRef, srcTransitionRef);
                }
            }
        }
    }

    private boolean isSignalAttribute(String value) {
        return INPUT_TYPE_ATTRIBUTE_VALUE.equals(value)
                || OUTPUT_TYPE_ATTRIBUTE_VALUE.equals(value)
                || INTERNAL_TYPE_ATTRIBUTE_VALUE.equals(value);
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
