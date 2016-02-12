package org.workcraft.plugins.serialisation.xml;

import java.io.File;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

public class FileSerialiser implements BasicXMLSerialiser {

    public String getClassName() {
        return File.class.getName();
    }

    public void serialise(Element element, Object object)
            throws SerialisationException {
        if (object != null) {
            File file = (File) object;
            String path = file.getAbsolutePath();
            element.setAttribute("path", path);
        }
    }

}
