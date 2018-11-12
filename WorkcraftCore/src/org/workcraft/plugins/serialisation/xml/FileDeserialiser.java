package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

import java.io.File;

public class FileDeserialiser implements BasicXMLDeserialiser<File> {

    @Override
    public String getClassName() {
        return File.class.getName();
    }

    @Override
    public File deserialise(Element element) {
        String path = element.getAttribute("path");
        return new File(path);
    }

}
