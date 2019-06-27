package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.references.AltFileReference;
import org.workcraft.serialisation.BasicXMLDeserialiser;

public class AltFileReferenceDeserialiser implements BasicXMLDeserialiser<AltFileReference> {

    @Override
    public String getClassName() {
        return AltFileReference.class.getName();
    }

    @Override
    public AltFileReference deserialise(Element element) {
        AltFileReference result = new AltFileReference();
        int index = 0;
        while (element.hasAttribute("path" + index)) {
            String path = element.getAttribute("path" + index);
            result.add(path);
            index++;
        }
        if (element.hasAttribute("path")) {
            result.setPath(element.getAttribute("path"));
        }
        return result;
    }

}
