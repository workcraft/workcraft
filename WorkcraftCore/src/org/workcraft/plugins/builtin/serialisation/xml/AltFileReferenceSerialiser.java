package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.references.AltFileReference;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.BasicXMLSerialiser;

public class AltFileReferenceSerialiser implements BasicXMLSerialiser<AltFileReference> {

    @Override
    public String getClassName() {
        return AltFileReference.class.getName();
    }

    @Override
    public void serialise(Element element, AltFileReference object) throws SerialisationException {
        if (object != null) {
            int index = 0;
            for (String path : object.getPaths()) {
                element.setAttribute("path" + index, path);
                index++;
            }
            String path = object.getPath();
            if (path != null) {
                element.setAttribute("path", path);
            }
        }
    }

}
