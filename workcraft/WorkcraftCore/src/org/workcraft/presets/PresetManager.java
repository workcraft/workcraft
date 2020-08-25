package org.workcraft.presets;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.workcraft.utils.*;
import org.workcraft.workspace.Resource;
import org.workcraft.workspace.WorkspaceEntry;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PresetManager<T> {

    public static final String PRESETS_ELEMENT_NAME = "presets";
    public static final String PRESET_ELEMENT_NAME = "preset";
    public static final String DESCRIPTION_ATTRIBUTE_NAME = "description";
    public static final String FILE_ATTRIBUTE_NAME = "file";

    private final WorkspaceEntry we;
    private final String key;
    private final ArrayList<Preset<T>> presets = new ArrayList<>();
    private final Set<Preset<T>> examplePresets = new HashSet<>();
    private final DataSerialiser<T> serialiser;
    private final Preset<T> preservedPreset;

    public PresetManager(WorkspaceEntry we, String key, DataSerialiser<T> serialiser, T preservedData) {
        this.we = we;
        this.key = key;
        this.serialiser = serialiser;
        // Add auto-preserved presets in the beginning of the list
        preservedPreset = preservedData == null ? null : addExamplePreset("Auto-preserved", preservedData);
        // Add user-defined presets after the auto-preserved one
        Resource resource = we.getResource(key);
        if (resource != null) {
            try {
                Document doc = XmlUtils.loadDocument(resource.toStream());
                for (Element presetElement : XmlUtils.getChildElements(PRESET_ELEMENT_NAME, doc.getDocumentElement())) {
                    Preset<T> preset = parsePreset(presetElement, serialiser);
                    presets.add(preset);
                }
            } catch (SAXException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        sortCustomPresets();
    }

    private Preset<T> parsePreset(Element presetElement, DataSerialiser<T> serialiser) {
        T data = serialiser.fromXML(presetElement, null);
        File file = null;
        if (presetElement.hasAttribute(FILE_ATTRIBUTE_NAME)) {
            file = new File(presetElement.getAttribute(FILE_ATTRIBUTE_NAME));
            data = overridePresetData(data, file);
        }
        String description = XmlUtils.readTextAttribute(presetElement, DESCRIPTION_ATTRIBUTE_NAME, "");
        Preset<T> preset = new Preset<>(description, data);
        preset.setFile(file);
        return preset;
    }

    private void sortCustomPresets() {
        int fromIndex = preservedPreset == null ? 0 : 1;
        int toIndex = fromIndex + presets.size() - examplePresets.size();
        List<Preset<T>> userPresets = presets.subList(fromIndex, toIndex);
        userPresets.sort(Comparator.comparing(Preset::getDescription));
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    public Preset<T> addExamplePreset(String description, T data) {
        Preset<T> preset = new Preset<>(description, data);
        presets.add(preset);
        examplePresets.add(preset);
        return preset;
    }

    public boolean isExamplePreset(Preset<T> preset) {
        return (preset != null) && examplePresets.contains(preset);
    }

    public boolean isPreservedPreset(Preset<T> preset) {
        return (preset != null) && (preset == preservedPreset);
    }

    public void updatePreset(Preset<T> preset, T data) {
        if (isExamplePreset(preset)) {
            throw new RuntimeException("Cannot overwrite an example preset");
        }
        File file = preset.getFile();
        preset.setData(overridePresetData(data, file));
        savePresets();
    }

    private T overridePresetData(T data, File file) {
        if (file != null) {
            try {
                String dataText = FileUtils.readAllText(file);
                Element presetElement = createPresetElement(null, dataText);
                data = serialiser.fromXML(presetElement, data);
            } catch (IOException e) {
                LogUtils.logError("Cannot read linked file '" + file.getAbsolutePath() + "'");
            }
        }
        return data;
    }

    public Preset<T> duplicatePreset(Preset<T> preset) {
        String description = preset.getDescription();
        Preset<T> oldPreset = getPresetByDescription(description);
        if (preset != oldPreset) {
            if (oldPreset == null) {
                int index = preservedPreset == null ? 0 : 1;
                presets.add(index, preset);
                savePresets();
                return preset;
            }
            if (canOverwritePreset(oldPreset)) {
                oldPreset.setData(preset.getData());
                savePresets();
                return oldPreset;
            }
        }
        return null;
    }

    public void renamePreset(Preset<T> preset, String description) {
        if (isExamplePreset(preset)) {
            throw new RuntimeException("Cannot rename example preset '" + preset.getDescription() + "'");
        }
        Preset<T> oldPreset = getPresetByDescription(description);
        if (preset != oldPreset) {
            if (oldPreset == null) {
                preset.setDescription(description);
                savePresets();
            } else if (canOverwritePreset(oldPreset)) {
                presets.remove(oldPreset);
                preset.setDescription(description);
                savePresets();
            }
        }
    }

    public void deletePreset(Preset<T> preset) {
        if (isExamplePreset(preset)) {
            throw new RuntimeException("Cannot delete example preset '" + preset.getDescription() + "'");
        }
        presets.remove(preset);
        savePresets();
    }

    private Preset<T> getPresetByDescription(String description) {
        for (Preset<T> preset : presets) {
            if (description.equals(preset.getDescription())) {
                return preset;
            }
        }
        return null;
    }

    private boolean canOverwritePreset(Preset<T> preset) {
        if (isExamplePreset(preset)) {
            DialogUtils.showError("Cannot overwrite example preset '" + preset.getDescription() + "'.");
            return false;
        }
        String msg = "Preset '" + preset.getDescription() + "' already exists.\nOverwrite?";
        return DialogUtils.showConfirm(msg, "Overwrite preset", false);
    }

    public List<Preset<T>> getPresets() {
        return Collections.unmodifiableList(presets);
    }

    private void savePresets() {
        sortCustomPresets();
        boolean isEmpty = true;
        Document doc = XmlUtils.createDocument();
        Element root = XmlUtils.createChildElement(PRESETS_ELEMENT_NAME, doc);
        for (Preset<T> preset : presets) {
            if (!isExamplePreset(preset)) {
                Element presetElement = XmlUtils.createChildElement(PRESET_ELEMENT_NAME, root);
                presetElement.setAttribute(DESCRIPTION_ATTRIBUTE_NAME, preset.getDescription());
                serialiser.toXML(preset.getData(), presetElement);
                File file = preset.getFile();
                if (file != null) {
                    presetElement.setAttribute(FILE_ATTRIBUTE_NAME, file.getAbsolutePath());
                }
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
    }

    public static Element createPresetElement(String description, String dataText) {
        Document presetDocument = XmlUtils.createDocument();
        Element presetElement = XmlUtils.createChildElement(PRESET_ELEMENT_NAME, presetDocument);
        presetElement.setAttribute(DESCRIPTION_ATTRIBUTE_NAME, description);
        if (TextUtils.isXmlElement(dataText)) {
            try {
                Document dataDocument = XmlUtils.createDocument(dataText);
                Element documentElement = dataDocument.getDocumentElement();
                Node dataNode = presetDocument.adoptNode(documentElement);
                presetElement.appendChild(dataNode);
            } catch (SAXException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            presetElement.setTextContent(dataText);
        }
        return presetElement;
    }

}
