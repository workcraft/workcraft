package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.serialisation.BasicXMLDeserialiser;

import java.io.File;

public class FileDeserialiser implements BasicXMLDeserialiser<File> {

    @Override
    public String getClassName() {
        return File.class.getName();
    }

    @Override
    public File deserialise(Element element) {
        return new File(element.getAttribute("path"));
    }

}
