package org.workcraft.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.types.GeneralTwoWayMap;
import org.workcraft.types.ListMap;
import org.workcraft.types.TwoWayMap;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

class XMLDeserialiserState implements References {
    private final ReferenceResolver externalReferences;
    private final GeneralTwoWayMap<String, Object> internalReferenceMap = new TwoWayMap<>();
    public HashMap<Object, Element> instanceElements = new HashMap<>();
    private final ListMap<Container, Node> children = new ListMap<>();

    XMLDeserialiserState(ReferenceResolver externalReferences) {
        this.externalReferences = externalReferences;
    }

    public ReferenceResolver getExternalReferences() {
        return externalReferences;
    }

    public References getInternalReferences() {
        return this;
    }

    public void addChildNode(Container parent, Node child) {
        children.put(parent, child);
    }

    public List<Node> getChildren(Container parent) {
        return children.get(parent);
    }

    public void setInstanceElement(Object instance, Element element) {
        instanceElements.put(instance, element);
    }

    public Element getInstanceElement(Object instance) {
        return instanceElements.get(instance);
    }

    public void setObject(String reference, Object obj) {
        String processedReference = NamespaceHelper.convertLegacyHierarchySeparators(reference);
        internalReferenceMap.put(processedReference, obj);
    }

    @Override
    public Object getObject(String reference) {
        if (reference.isEmpty()) return null;
        String processedReference = NamespaceHelper.convertLegacyHierarchySeparators(reference);
        return internalReferenceMap.getValue(processedReference);
    }

    @Override
    public String getReference(Object obj) {
        return internalReferenceMap.getKey(obj);
    }

    @Override
    public Set<Object> getObjects() {
        return internalReferenceMap.getValues();
    }

    @Override
    public Set<String> getReferences() {
        return internalReferenceMap.getKeys();
    }

}
