package org.workcraft.plugins.shared.presets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class PresetManager<T> {
    private final ArrayList<Preset<T>> presets = new ArrayList<>();
    private final File presetFile;
    private final SettingsSerialiser<T> serialiser;

    public PresetManager(File presetFile, SettingsSerialiser<T> serialiser) {
        this.presetFile = presetFile;
        this.serialiser = serialiser;

        try {
            if (presetFile.exists()) {
                Document doc = XmlUtil.loadDocument(presetFile);
                for (Element p : XmlUtil.getChildElements("preset", doc.getDocumentElement())) {
                    presets.add(new Preset<T>(p, serialiser));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void savePresets() {
        try {
            Document doc = XmlUtil.createDocument();

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

            XmlUtil.saveDocument(doc, presetFile);
        } catch (ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sort() {
        Collections.sort(presets, new Comparator<Preset<T>>() {
            @Override
            public int compare(Preset<T> o1, Preset<T> o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
    }

    public void add(Preset<T> preset) {
        presets.add(preset);
    }

    public Preset<T> save(T settings, String description) {
        Preset<T> preset = new Preset<>(description, settings, false);
        presets.add(0, preset);
        savePresets();
        return preset;
    }

    public void update(Preset<T> preset, T settings) {
        checkBuiltIn(preset);
        preset.setSettings(settings);
        savePresets();
    }

    public void delete(Preset<T> preset) {
        checkBuiltIn(preset);
        presets.remove(preset);
        savePresets();
    }

    private void checkBuiltIn(Preset<T> preset) {
        if (preset.isBuiltIn()) {
            throw new RuntimeException("Invalid operation attempted on a built-in preset.");
        }
    }

    public Preset<T> find(String description) {
        for (Preset<T> p : presets) {
            if (p.getDescription().equals(description)) {
                return p;
            }
        }
        return null;
    }

    public void rename(Preset<T> preset, String description) {
        checkBuiltIn(preset);
        preset.setDescription(description);
        savePresets();
    }

    public List<Preset<T>> list() {
        return Collections.unmodifiableList(presets);
    }
}
