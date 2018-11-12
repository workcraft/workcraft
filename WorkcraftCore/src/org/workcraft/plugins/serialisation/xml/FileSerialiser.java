package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

import java.io.File;

public class FileSerialiser implements BasicXMLSerialiser<File> {

    @Override
    public String getClassName() {
        return File.class.getName();
    }

    @Override
    public void serialise(Element element, File object) throws SerialisationException {
        if (object != null) {
            String path = object.getAbsolutePath();
            element.setAttribute("path", path);
        }
    }

}
