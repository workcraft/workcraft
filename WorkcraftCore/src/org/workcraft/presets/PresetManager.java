package org.workcraft.presets;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.utils.XmlUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresetManager<T> {

    private final ArrayList<Preset<T>> presets = new ArrayList<>();
    private final File presetFile;
    private final SettingsSerialiser<T> serialiser;

    public PresetManager(File presetFile, SettingsSerialiser<T> serialiser) {
        this.presetFile = presetFile;
        this.serialiser = serialiser;

        try {
            if (presetFile.exists()) {
                Document doc = XmlUtils.loadDocument(presetFile);
                for (Element p : XmlUtils.getChildElements("preset", doc.getDocumentElement())) {
                    presets.add(new Preset<>(p, serialiser));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFirst(List<Preset<T>> otherPresets) {
        presets.addAll(0, otherPresets);
    }

    public void savePreset(Preset<T> preset) {
        presets.add(preset);
        savePresets();
    }

    public void updatePreset(Preset<T> preset, T settings) {
        checkBuiltIn(preset);
        preset.setSettings(settings);
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
            Document doc = XmlUtils.createDocument();

            Element root = doc.createElement("presets");
            doc.appendChild(root);

            for (Preset<T> p : presets) {
                if (!p.isBuiltIn()) {
                    Element pe = doc.createElement("preset");
                    pe.setAttribute("description", p.getDescription());
                    serialiser.toXML(p.getSettings(), pe);
                    root.appendChild(pe);
                }
            }

            XmlUtils.saveDocument(doc, presetFile);
        } catch (ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkBuiltIn(Preset<T> preset) {
        if (preset.isBuiltIn()) {
            throw new RuntimeException("Invalid operation attempted on a built-in preset.");
        }
    }
}
