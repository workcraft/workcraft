package org.workcraft.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.Version;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.visual.NodeHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.builtin.serialisation.XMLModelSerialiser;
import org.workcraft.serialisation.*;
import org.workcraft.shared.DataAccumulator;
import org.workcraft.workspace.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class WorkUtils {

    private static final String META_WORK_ENTRY = "meta";
    private static final String STATE_WORK_ENTRY = "state.xml";
    private static final String MATH_MODEL_WORK_ENTRY = "model.xml";
    private static final String VISUAL_MODEL_WORK_ENTRY = "visualModel.xml";
    private static final String RESOURCES_WORK_ENTRY = "resources/";

    private static final String META_WORK_ELEMENT = "workcraft-meta";
    private static final String META_DESCRIPTOR_WORK_ELEMENT = "descriptor";
    private static final String META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE = "class";
    private static final String META_STAMP_WORK_ELEMENT = "stamp";
    private static final String META_STAMP_TIME_WORK_ATTRIBUTE = "time";
    private static final String META_STAMP_UUID_WORK_ATTRIBUTE = "uuid";
    private static final String META_MATH_MODEL_WORK_ELEMENT = "math";
    private static final String META_VISUAL_MODEL_WORK_ELEMENT = "visual";
    private static final String META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE = "entry-name";
    private static final String META_MODEL_FORMAT_UUID_WORK_ATTRIBUTE = "format-uuid";
    private static final String META_VERSION_WORK_ELEMENT = "version";
    private static final String META_VERSION_MAJOR_WORK_ATTRIBUTE = "major";
    private static final String META_VERSION_MINOR_WORK_ATTRIBUTE = "minor";
    private static final String META_VERSION_REVISION_WORK_ATTRIBUTE = "revision";
    private static final String META_VERSION_STATUS_WORK_ATTRIBUTE = "status";
    private static final Pattern META_VERSION_PATTERN = Pattern.compile(
            "<" + META_VERSION_WORK_ELEMENT + " "
            + META_VERSION_MAJOR_WORK_ATTRIBUTE + "=\"([0-9]+)\" "
            + META_VERSION_MINOR_WORK_ATTRIBUTE + "=\"([0-9]+)\" "
            + META_VERSION_REVISION_WORK_ATTRIBUTE + "=\"([0-9]+)\" "
            + META_VERSION_STATUS_WORK_ATTRIBUTE + "=\"(.*)\"/>");

    private static final String STATE_WORK_ELEMENT = "workcraft-state";
    private static final String STATE_LEVEL_WORK_ELEMENT = "level";
    private static final String STATE_SELECTION_WORK_ELEMENT = "selection";

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("<([A-Z]\\S*).*>");
    private static final Pattern MODEL_NAME_PATTERN = Pattern.compile(
            "<model " + XMLCommonAttributes.CLASS_ATTRIBUTE + "=\"(.+?)\" "
            + XMLCommonAttributes.REF_ATTRIBUTE + "=\"\">");


    public static ModelEntry cloneModel(ModelEntry me) {
        return loadModel(mementoModel(me));
    }

    public static Resource mementoModel(ModelEntry me) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            saveModel(me, null, os);
        } catch (SerialisationException e) {
            throw new RuntimeException(e);
        }
        return new Resource("memento", os);
    }

    public static ModelEntry loadModel(Resource memento) {
        try {
            return loadModel(memento.toStream());
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    public static ModelEntry loadModel(File file) throws DeserialisationException {
        Framework framework = Framework.getInstance();
        WorkspaceEntry we = framework.getWorkspace().getWork(file);
        if (we != null) {
            return cloneModel(we.getModelEntry());
        }
        ModelEntry me = null;
        if (FileUtils.checkAvailability(file, null, false)) {
            // Load (from *.work) or import (other extensions) work.
            if (FileFilters.isWorkFile(file)) {
                try {
                    CompatibilityManager cm = framework.getCompatibilityManager();
                    InputStream is = cm.process(file);
                    me = loadModel(is);
                    String base = FileUtils.getBasePath(file);
                    adjustPropertyFilePaths(me.getVisualModel(), base, true);
                } catch (OperationCancelledException e) {
                    // Operation cancelled by the user
                }
            } else {
                try {
                    Importer importer = ImportUtils.chooseBestImporter(file);
                    if (importer == null) {
                        throw new DeserialisationException("Cannot identify appropriate importer for file '" + file.getAbsolutePath() + "'");
                    }
                    me = ImportUtils.importFromFile(importer, file);
                } catch (IOException e) {
                    throw new DeserialisationException(e);
                } catch (OperationCancelledException e) {
                    // Operation cancelled by the user
                }
            }
        }
        return me;
    }

    public static ModelEntry loadModel(InputStream is) throws DeserialisationException {
        try {
            // Buffer the whole stream in a byte array
            byte[] bytes = DataAccumulator.loadStream(is);

            // Load meta data
            Document metaDocument = loadMetaDoc(bytes);

            // Load math model
            DeserialisationResult mathResult = deserialiseMathModel(bytes, metaDocument);
            if (mathResult == null) {
                throw new DeserialisationException("Math model is missing");
            }
            Model model = mathResult.model;
            model.afterDeserialisation();

            // Load visual model (if present)
            DeserialisationResult visualResult = deserialiseVisualModel(bytes, metaDocument, mathResult);
            if (visualResult != null) {
                model = visualResult.model;
                model.afterDeserialisation();
                // Load current level and selection
                if (model instanceof VisualModel) {
                    References visualRefs = visualResult.references;
                    loadSelectionState(bytes, (VisualModel) model, visualRefs);
                }
            }
            // Create model entry
            ModelDescriptor descriptor = loadMetaDescriptor(metaDocument);
            ModelEntry me = new ModelEntry(descriptor, model);

            // Load version and time stamp
            Version version = loadMetaVersion(metaDocument);
            Stamp stamp = loadMetaStamp(metaDocument);
            me.setVersion(version);
            me.setStamp(stamp);
            return me;
        } catch (IOException e) {
            throw new DeserialisationException(e);
        }
    }

    public static ModelEntry loadModel(InputStream is1, InputStream is2) throws DeserialisationException {
        ModelEntry me1 = loadModel(is1);
        ModelEntry me2 = loadModel(is2);

        String displayName1 = me1.getDescriptor().getDisplayName();
        String displayName2 = me2.getDescriptor().getDisplayName();
        if (!displayName1.equals(displayName2)) {
            throw new DeserialisationException(
                    "Incompatible " + displayName1 + " and " + displayName2 + " model cannot be merged.");
        }

        VisualModel vmodel1 = me1.getVisualModel();
        VisualModel vmodel2 = me2.getVisualModel();
        Collection<VisualNode> children = NodeHelper.filterByType(vmodel2.getRoot().getChildren(), VisualNode.class);

        vmodel1.selectNone();
        if (vmodel1.reparent(vmodel1.getCurrentLevel(), vmodel2, vmodel2.getRoot(), null)) {
            vmodel1.select(children);
        }
        // FIXME: Dirty hack to avoid any hanging observers (serialise and deserialise the model).
        return cloneModel(me1);
    }

    private static DeserialisationResult deserialiseMathModel(byte[] bytes, Document document)
            throws IOException, DeserialisationException {

        Element element = XmlUtils.getChildElement(META_MATH_MODEL_WORK_ELEMENT, document.getDocumentElement());
        if (element != null) {
            try (InputStream mathData = getZipEntry(element.getAttribute(META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE), bytes)) {
                PluginManager pm = Framework.getInstance().getPluginManager();
                XMLModelDeserialiser mathDeserialiser = new XMLModelDeserialiser(pm);
                return mathDeserialiser.deserialise(mathData, null, null);
            }
        }
        return null;
    }

    private static DeserialisationResult deserialiseVisualModel(byte[] bytes, Document document, DeserialisationResult mathResult)
            throws IOException, DeserialisationException {

        Element element = XmlUtils.getChildElement(META_VISUAL_MODEL_WORK_ELEMENT, document.getDocumentElement());
        if (element  != null) {
            try (InputStream visualData = getZipEntry(element.getAttribute(META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE), bytes)) {
                if (visualData != null) {
                    PluginManager pm = Framework.getInstance().getPluginManager();
                    XMLModelDeserialiser visualDeserialiser = new XMLModelDeserialiser(pm);
                    return visualDeserialiser.deserialise(visualData, mathResult.references, mathResult.model);
                }
            }
        }
        return null;
    }

    private static ModelDescriptor loadMetaDescriptor(Document document) throws DeserialisationException {
        Element element = XmlUtils.getChildElement(META_DESCRIPTOR_WORK_ELEMENT, document.getDocumentElement());
        String descriptorClass = element.getAttribute(META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE);
        try {
            return (ModelDescriptor) Class.forName(descriptorClass).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new DeserialisationException("Cannot load meta descriptor", e);
        }
    }

    private static Version loadMetaVersion(Document document) {
        Element element = XmlUtils.getChildElement(META_VERSION_WORK_ELEMENT, document.getDocumentElement());
        if (element != null) {
            String major = element.getAttribute(META_VERSION_MAJOR_WORK_ATTRIBUTE);
            String minor = element.getAttribute(META_VERSION_MINOR_WORK_ATTRIBUTE);
            String revision = element.getAttribute(META_VERSION_REVISION_WORK_ATTRIBUTE);
            String status = element.getAttribute(META_VERSION_STATUS_WORK_ATTRIBUTE);
            return new Version(major, minor, revision, status);
        }
        return null;
    }

    private static Stamp loadMetaStamp(Document document) {
        Element element = XmlUtils.getChildElement(META_STAMP_WORK_ELEMENT, document.getDocumentElement());
        if (element != null) {
            String time = element.getAttribute(META_STAMP_TIME_WORK_ATTRIBUTE);
            String uuid = element.getAttribute(META_STAMP_UUID_WORK_ATTRIBUTE);
            if ((time != null) && (uuid != null)) {
                return new Stamp(time, uuid);
            }
        }
        return null;
    }

    private static Document loadMetaDoc(byte[] bytes) throws DeserialisationException {
        try (InputStream metaData = getZipEntry(META_WORK_ENTRY, bytes)) {
            if (metaData == null) {
                throw new DeserialisationException("Meta entry is missing");
            }
            return XmlUtils.loadDocument(metaData);
        } catch (SAXException | IOException e) {
            throw new DeserialisationException("Cannot load meta entry", e);
        }
    }

    private static void loadSelectionState(byte[] bytes, VisualModel model, References references)
            throws DeserialisationException {

        try (InputStream stateData = getZipEntry(STATE_WORK_ENTRY, bytes)) {
            if (stateData != null) {
                Document stateDoc = XmlUtils.loadDocument(stateData);
                Element stateElement = stateDoc.getDocumentElement();
                // Load current level
                Element levelElement = XmlUtils.getChildElement(STATE_LEVEL_WORK_ELEMENT, stateElement);
                Object currentLevel = references.getObject(levelElement.getAttribute(XMLCommonAttributes.REF_ATTRIBUTE));
                if (currentLevel instanceof Container) {
                    model.setCurrentLevel((Container) currentLevel);
                }
                // Load selection
                Element selectionElement = XmlUtils.getChildElement(STATE_SELECTION_WORK_ELEMENT, stateElement);
                Set<VisualNode> nodes = new HashSet<>();
                for (Element nodeElement : XmlUtils.getChildElements(XMLCommonAttributes.NODE_ATTRIBUTE, selectionElement)) {
                    Object node = references.getObject(nodeElement.getAttribute(XMLCommonAttributes.REF_ATTRIBUTE));
                    if (node instanceof VisualNode) {
                        nodes.add((VisualNode) node);
                    }
                }
                model.addToSelection(nodes);
            }
        } catch (IOException | SAXException e) {
            throw new DeserialisationException("Cannot load selection state", e);
        }
    }

    private static InputStream getZipEntry(String name, byte[] bytes) throws IOException {
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.getName().equals(name)) {
                return zis;
            }
            zis.closeEntry();
        }
        zis.close();
        return null;
    }

    public static Collection<Resource> loadResources(File file) throws DeserialisationException {
        try (InputStream is = new FileInputStream(file)) {
            Collection<Resource> resources = loadResources(is);
            String base = FileUtils.getBasePath(file);
            return adjustResourceFilePaths(resources, base, true);
        } catch (IOException e) {
            throw new DeserialisationException(e);
        }
    }

    private static Collection<Resource> loadResources(InputStream is) throws IOException {
        byte[] bytes = DataAccumulator.loadStream(is);
        Collection<Resource> result = new ArrayList<>();
        ByteArrayInputStream zippedData = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(zippedData, StandardCharsets.UTF_8);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            String name = ze.getName();
            if (name.startsWith(RESOURCES_WORK_ENTRY) && !RESOURCES_WORK_ENTRY.equals(name)) {
                String key = name.substring(RESOURCES_WORK_ENTRY.length());
                result.add(new Resource(key, zis));
            }
            zis.closeEntry();
        }
        zis.close();
        return result;
    }

    public static void saveModel(ModelEntry me, Collection<Resource> resources, File file)
            throws SerialisationException {

        try {
            FileOutputStream os = new FileOutputStream(file);
            String base = FileUtils.getBasePath(file);
            adjustPropertyFilePaths(me.getVisualModel(), base, false);
            Collection<Resource> adjustedResources = adjustResourceFilePaths(resources, base, false);
            saveModel(me, adjustedResources, os);
            os.close();
        } catch (IOException e) {
            throw new SerialisationException(e);
        } finally {
            adjustPropertyFilePaths(me.getVisualModel(), null, true);
        }
    }

    public static void saveModel(ModelEntry me, Collection<Resource> resources, OutputStream os)
            throws SerialisationException {

        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            final PluginManager pm = Framework.getInstance().getPluginManager();
            ModelSerialiser serialiser = new XMLModelSerialiser(pm);
            // Save math model
            Model mathModel = me.getMathModel();
            if (mathModel != null) {
                zos.putNextEntry(new ZipEntry(MATH_MODEL_WORK_ENTRY));
                mathModel.beforeSerialisation();
                ReferenceProducer refResolver = serialiser.serialise(mathModel, zos, null);
                zos.closeEntry();

                // Save visual model
                VisualModel visualModel = me.getVisualModel();
                if (visualModel != null) {
                    zos.putNextEntry(new ZipEntry(VISUAL_MODEL_WORK_ENTRY));
                    visualModel.beforeSerialisation();
                    ReferenceProducer visualRefs = serialiser.serialise(visualModel, zos, refResolver);
                    zos.closeEntry();
                    // Serialise visual model selection state
                    zos.putNextEntry(new ZipEntry(STATE_WORK_ENTRY));
                    saveSelectionState(visualModel, zos, visualRefs);
                    zos.closeEntry();
                }
            }

            // Save meta data
            zos.putNextEntry(new ZipEntry(META_WORK_ENTRY));
            String uuid = serialiser.getFormatUUID().toString();
            saveMeta(me, zos, uuid);
            zos.closeEntry();

            // Save resources
            if (resources != null) {
                for (Resource resource : resources) {
                    String name = RESOURCES_WORK_ENTRY + resource.getName();
                    zos.putNextEntry(new ZipEntry(name));
                    zos.write(resource.toByteArray());
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            throw new SerialisationException(e);
        }
    }

    private static void saveSelectionState(VisualModel visualModel, OutputStream os, ReferenceProducer visualRefs) {
        Document stateDocument = XmlUtils.createDocument();
        Element stateRoot = stateDocument.createElement(STATE_WORK_ELEMENT);
        stateDocument.appendChild(stateRoot);
        // level
        Element levelElement = stateDocument.createElement(STATE_LEVEL_WORK_ELEMENT);
        String currentLevelRef = visualRefs.getReference(visualModel.getCurrentLevel());
        levelElement.setAttribute(XMLCommonAttributes.REF_ATTRIBUTE, currentLevelRef);
        stateRoot.appendChild(levelElement);
        // selection
        Element selectionElement = stateDocument.createElement(STATE_SELECTION_WORK_ELEMENT);
        for (Node node: visualModel.getSelection()) {
            Element nodeElement = stateDocument.createElement(XMLCommonAttributes.NODE_ATTRIBUTE);
            String ref = visualRefs.getReference(node);
            nodeElement.setAttribute(XMLCommonAttributes.REF_ATTRIBUTE, ref);
            selectionElement.appendChild(nodeElement);
        }
        stateRoot.appendChild(selectionElement);
        XmlUtils.writeDocument(stateDocument, os);
    }

    private static void saveMeta(ModelEntry modelEntry, OutputStream os, String uuid) {
        Document metaDocument = XmlUtils.createDocument();
        Element metaRoot = metaDocument.createElement(META_WORK_ELEMENT);
        metaDocument.appendChild(metaRoot);

        Element metaVersion = metaDocument.createElement(META_VERSION_WORK_ELEMENT);
        metaVersion.setAttribute(META_VERSION_MAJOR_WORK_ATTRIBUTE, Info.getVersionMajor());
        metaVersion.setAttribute(META_VERSION_MINOR_WORK_ATTRIBUTE, Info.getVersionMinor());
        metaVersion.setAttribute(META_VERSION_REVISION_WORK_ATTRIBUTE, Info.getVersionRevision());
        metaVersion.setAttribute(META_VERSION_STATUS_WORK_ATTRIBUTE, Info.getVersionStatus());
        metaRoot.appendChild(metaVersion);

        Element metaStamp = metaDocument.createElement(META_STAMP_WORK_ELEMENT);
        Stamp stamp = modelEntry.getStamp();
        metaStamp.setAttribute(META_STAMP_TIME_WORK_ATTRIBUTE, stamp.time);
        metaStamp.setAttribute(META_STAMP_UUID_WORK_ATTRIBUTE, stamp.uuid);
        metaRoot.appendChild(metaStamp);

        Element metaDescriptor = metaDocument.createElement(META_DESCRIPTOR_WORK_ELEMENT);
        String descriptorClass = modelEntry.getDescriptor().getClass().getCanonicalName();
        metaDescriptor.setAttribute(META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE, descriptorClass);
        metaRoot.appendChild(metaDescriptor);

        Element mathElement = metaDocument.createElement(META_MATH_MODEL_WORK_ELEMENT);
        mathElement.setAttribute(META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE, MATH_MODEL_WORK_ENTRY);
        mathElement.setAttribute(META_MODEL_FORMAT_UUID_WORK_ATTRIBUTE, uuid);
        metaRoot.appendChild(mathElement);

        if (modelEntry.getVisualModel() != null) {
            Element visualElement = metaDocument.createElement(META_VISUAL_MODEL_WORK_ELEMENT);
            visualElement.setAttribute(META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE, VISUAL_MODEL_WORK_ENTRY);
            visualElement.setAttribute(META_MODEL_FORMAT_UUID_WORK_ATTRIBUTE, uuid);
            metaRoot.appendChild(visualElement);
        }
        XmlUtils.writeDocument(metaDocument, os);
    }

    private static void adjustPropertyFilePaths(VisualModel model, String base, boolean absolute) {
        Set<PropertyDescriptor> properties = new HashSet<>();
        properties.addAll(model.getProperties(null).getDescriptors());
        for (VisualNode node : Hierarchy.getDescendantsOfType(model.getRoot(), VisualNode.class)) {
            properties.addAll(node.getDescriptors());
            properties.addAll(model.getProperties(node).getDescriptors());
        }
        for (PropertyDescriptor property : properties) {
            adjustPropertyFilePath(property, base, absolute);
        }
    }

    private static void adjustPropertyFilePath(PropertyDescriptor property, String base, boolean absolute) {
        Object value = property.getValue();
        if (value instanceof FileReference) {
            FileReference fileReference = (FileReference) value;
            fileReference.setBase(base);
            if (absolute) {
                fileReference.setBase(null);
            }
        }
    }

    private static Collection<Resource> adjustResourceFilePaths(Collection<Resource> resources, String base, boolean absolute) {
        Collection<Resource> result = new HashSet<>();
        for (Resource resource : resources) {
            try {
                result.add(adjustResourceFilePath(resource, base, absolute));
            } catch (IOException e) {
                LogUtils.logError("Failed loading resource '" + resource.getName() + "'");
            }
        }
        return result;
    }

    private static Resource adjustResourceFilePath(Resource resource, String base, boolean absolute) throws IOException {
        Pattern pattern = Pattern.compile(Resource.FILE_ATTRIBUTE_SUFFIX + "\\s*=\\s*\"(.+)\"");
        InputStream is = new ByteArrayInputStream(resource.toByteArray());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            line += "\n";
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String path = matcher.group(1);
                FileReference fileReference = new FileReference();
                fileReference.setPath(path);
                fileReference.setBase(base);
                if (absolute) {
                    fileReference.setBase(null);
                }
                String attribute = matcher.group();
                String adjustedAttribute = attribute.replace(path, fileReference.getPath());
                line = line.replace(attribute, adjustedAttribute);
            }
            os.write(line.getBytes());
        }
        return new Resource(resource.getName(), os);
    }

    public static Version extractVersion(ZipFile zipFile) throws IOException {
        ZipEntry metaZipEntry = zipFile.getEntry(META_WORK_ENTRY);
        if (metaZipEntry != null) {
            InputStream inputStream = zipFile.getInputStream(metaZipEntry);
            return extractVersion(inputStream);
        }
        return null;
    }

    private static Version extractVersion(InputStream is) throws IOException {
        Version result = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line = null;
        while ((result == null) && (line = reader.readLine()) != null) {
            result = extractVersion(line);
        }
        return result;
    }

    private static Version extractVersion(String line) {
        Version result = null;
        Matcher matcher = META_VERSION_PATTERN.matcher(line);
        if (matcher.find()) {
            String major = matcher.group(1);
            String minor = matcher.group(2);
            String revision = matcher.group(3);
            String status = matcher.group(4);
            result = new Version(major, minor, revision, status);
        }
        return result;
    }

    public static String extractModelName(String line) {
        String result = null;
        Matcher matcher = MODEL_NAME_PATTERN.matcher(line);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public static String extractClassName(String line) {
        String result = null;
        Matcher matcher = CLASS_NAME_PATTERN.matcher(line);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public static boolean isMetaEntry(ZipEntry ze) {
        return (ze != null) && META_WORK_ENTRY.equals(ze.getName());
    }

}
