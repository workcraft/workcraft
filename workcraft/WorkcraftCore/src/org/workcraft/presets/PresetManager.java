package org.workcraft.presets;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.XmlUtils;
import org.workcraft.workspace.Resource;
import org.workcraft.workspace.WorkspaceEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PresetManager<T> {

    public static final String PRESETS_ELEMENT_NAME = "presets";
    public static final String PRESET_ELEMENT_NAME = "preset";
    public static final String DESCRIPTION_ATTRIBUTE_NAME = "description";

    private static final String AUTO_PRESERVE_PREFIX = "\u2713 ";
    private static final String EXAMPLE_PRESET_PREFIX = "\u00BB ";

    private final WorkspaceEntry we;
    private final String key;
    private final ArrayList<Preset<T>> presets = new ArrayList<>();
    private final DataSerialiser<T> serialiser;
    private final Preset<T> preservedPreset;

    public PresetManager(WorkspaceEntry we, String key, DataSerialiser<T> serialiser, T preservedData) {
        this.we = we;
        this.key = key;
        this.serialiser = serialiser;
        // Add auto-preserved presets in the beginning of the list
        if (preservedData == null) {
            preservedPreset = null;
        } else {
            preservedPreset = new Preset<>(AUTO_PRESERVE_PREFIX + "Auto-preserved",
                    preservedData, true);

            presets.add(preservedPreset);
        }
        // Add user-defined presets after the auto-preserved one
        Resource resource = we.getResource(key);
        if (resource != null) {
            try {
                Document doc = XmlUtils.loadDocument(resource.toStream());
                for (Element element : XmlUtils.getChildElements(PRESET_ELEMENT_NAME, doc.getDocumentElement())) {
                    String description = element.getAttribute(DESCRIPTION_ATTRIBUTE_NAME);
                    T data = serialiser.fromXML(element);
                    presets.add(new Preset<>(description, data, false));
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        sortUserPresets();
    }

    private void sortUserPresets() {
        if (preservedPreset == null) {
            presets.sort(Comparator.comparing(Preset::toString));
        } else {
            Collections.sort(presets.subList(1, presets.size()), Comparator.comparing(Preset::toString));
        }
    }

    public void addExample(String description, T data) {
        presets.add(new Preset<>(EXAMPLE_PRESET_PREFIX + description, data, true));
    }

    public Preset savePreset(Preset<T> newPreset) {
        Preset oldPreset = null;
        for (Preset preset : presets) {
            if (!preset.isBuiltIn() && preset.toString().equals(newPreset.toString())) {
                oldPreset = preset;
                break;
            }
        }
        Preset savedPreset = null;
        if (oldPreset == null) {
            savedPreset = newPreset;
            presets.add(savedPreset);
            savePresets();
        } else {
            String msg = "Preset \'" + newPreset.getDescription() + "\' already exists.\nOverwrite?";
            if (DialogUtils.showConfirm(msg, "Overwrite preset", false)) {
                savedPreset = oldPreset;
                updatePreset(savedPreset, newPreset.getData());
            }
        }
        return savedPreset;
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
        sortUserPresets();
        try {
            boolean isEmpty = true;
            Document doc = XmlUtils.createDocument();
            Element root = doc.createElement(PRESETS_ELEMENT_NAME);
            doc.appendChild(root);
            for (Preset<T> preset : presets) {
                if (!preset.isBuiltIn()) {
                    Element pe = doc.createElement(PRESET_ELEMENT_NAME);
                    pe.setAttribute(DESCRIPTION_ATTRIBUTE_NAME, preset.getDescription());
                    serialiser.toXML(preset.getData(), pe);
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
