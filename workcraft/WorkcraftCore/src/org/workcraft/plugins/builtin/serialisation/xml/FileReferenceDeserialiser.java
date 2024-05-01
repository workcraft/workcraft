package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.references.FileReference;
import org.workcraft.serialisation.BasicXMLDeserialiser;

public class FileReferenceDeserialiser implements BasicXMLDeserialiser<FileReference> {

    @Override
    public String getClassName() {
        return FileReference.class.getName();
    }

    @Override
    public FileReference deserialise(Element element) {
        FileReference result = null;
        if (element.hasAttribute("path")) {
            result = new FileReference(element.getAttribute("path"));
        }
        return result;
    }

}
