package org.workcraft.presets;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.XmlUtils;
import org.workcraft.workspace.Resource;
import org.workcraft.workspace.WorkspaceEntry;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataPreserver<T> {

    private final WorkspaceEntry we;
    private final String key;
    private final DataSerialiser<T> serialiser;

    public DataPreserver(WorkspaceEntry we, String key, DataSerialiser<T> serialiser) {
        this.we = we;
        this.key = key;
        this.serialiser = serialiser;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    public T loadData() {
        Document doc = null;
        Resource resource = we.getResource(key);
        if (resource != null) {
            try {
                doc = XmlUtils.loadDocument(resource.toStream());
            } catch (SAXException | IOException e) {
                LogUtils.logError("Failed loading data from resource '" + resource.getName() + "'");
            }
        }
        Element root = doc == null ? null : doc.getDocumentElement();
        return serialiser.fromXML(root, null);
    }

    public void saveData(T data) {
        if (data == null) {
            if (we.removeResource(key) != null) {
                we.setChanged(true);
            }
        } else {
            T oldData = loadData();
            if (!data.equals(oldData)) {
                Document doc = XmlUtils.createDocument();
                Element root = XmlUtils.createChildElement("root", doc);
                serialiser.toXML(data, root);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                XmlUtils.writeDocument(doc, os);
                we.addResource(new Resource(key, os));
                we.setChanged(true);
            }
        }
    }

}
