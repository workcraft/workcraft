package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.references.FileReference;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.BasicXMLSerialiser;

public class FileReferenceSerialiser implements BasicXMLSerialiser<FileReference> {

    @Override
    public String getClassName() {
        return FileReference.class.getName();
    }

    @Override
    public void serialise(Element element, FileReference object) throws SerialisationException {
        if (object != null) {
            String path = object.getPath();
            if (path != null) {
                element.setAttribute("path", path);
            }
        }
    }

}
