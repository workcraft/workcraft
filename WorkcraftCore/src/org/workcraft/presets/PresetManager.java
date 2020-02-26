package org.workcraft.presets;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.utils.XmlUtils;
import org.workcraft.workspace.Resource;
import org.workcraft.workspace.WorkspaceEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresetManager<T> {

    public static final String PRESETS_ELEMENT_NAME = "presets";
    public static final String PRESET_ELEMENT_NAME = "preset";
    public static final String DESCRIPTION_ATTRIBUTE_NAME = "description";

    private final WorkspaceEntry we;
    private final String key;
    private final ArrayList<Preset<T>> presets = new ArrayList<>();
    private final DataSerialiser<T> serialiser;

    public PresetManager(WorkspaceEntry we, String key, DataSerialiser<T> serialiser, T preservedData) {
        this.we = we;
        this.key = key;
        this.serialiser = serialiser;
        if (preservedData != null) {
            presets.add(new Preset<>("Auto-preserved", preservedData, true));
        }
        try {
            Resource data = we.getResource(key);
            if (data != null) {
                Document doc = XmlUtils.loadDocument(data.toStream());
                for (Element p : XmlUtils.getChildElements(PRESET_ELEMENT_NAME, doc.getDocumentElement())) {
                    presets.add(new Preset<>(p, serialiser));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(Preset<T> preset) {
        presets.add(preset);
    }

    public void savePreset(Preset<T> preset) {
        presets.add(preset);
        savePresets();
    }

    public void updatePreset(Preset<T> preset, T data) {
        checkBuiltIn(preset);
        preset.setData(data);
        savePresets();
    }

    public void removePreset(Preset<T> preset) {
        checkBuiltIn(preset);
        presets.remove(preset);
        savePresets();
    }

    public void renamePreset(Preset<T> preset, String description) {
        checkBuiltIn(preset);
        preset.setDescription(description);
        savePresets();
    }

    public List<Preset<T>> getPresets() {
        return Collections.unmodifiableList(presets);
    }

    private void savePresets() {
        try {
            boolean isEmpty = true;
            Document doc = XmlUtils.createDocument();
            Element root = doc.createElement(PRESETS_ELEMENT_NAME);
            doc.appendChild(root);
            for (Preset<T> p : presets) {
                if (!p.isBuiltIn()) {
                    Element pe = doc.createElement(PRESET_ELEMENT_NAME);
                    pe.setAttribute(DESCRIPTION_ATTRIBUTE_NAME, p.getDescription());
                    serialiser.toXML(p.getData(), pe);
                    root.appendChild(pe);
                    isEmpty = false;
                }
            }
            if (isEmpty) {
                we.removeResource(key);
            } else {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                XmlUtils.writeDocument(doc, os);
                we.addResource(new Resource(key, os));
            }
            we.setChanged(true);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkBuiltIn(Preset<T> preset) {
        if (preset.isBuiltIn()) {
            throw new RuntimeException("Invalid operation attempted on a built-in preset.");
        }
    }

}
