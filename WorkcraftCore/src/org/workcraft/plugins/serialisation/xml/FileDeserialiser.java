package org.workcraft.plugins.serialisation.xml;

import java.io.File;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class FileDeserialiser implements BasicXMLDeserialiser{

    public String getClassName() {
        return File.class.getName();
    }

    public Object deserialise(Element element) throws DeserialisationException {
        String path = element.getAttribute("path");
        return new File(path);
    }

}
