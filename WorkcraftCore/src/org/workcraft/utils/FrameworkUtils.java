package org.workcraft.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.Version;
import org.workcraft.dom.Container;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.References;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.RawData;
import org.workcraft.workspace.Stamp;
import org.workcraft.workspace.Storage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FrameworkUtils {

    public static InputStream getMathData(byte[] bytes, Document document) throws IOException {
        Element element = XmlUtils.getChildElement(Framework.META_MATH_MODEL_WORK_ELEMENT, document.getDocumentElement());
        if (element != null) {
            InputStream is = new ByteArrayInputStream(bytes);
            return ZipUtils.getUncompressedEntry(element.getAttribute(Framework.META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE), is);
        }
        return null;
    }

    public static InputStream getVisualData(byte[] bytes, Document document) throws IOException {
        Element element = XmlUtils.getChildElement(Framework.META_VISUAL_MODEL_WORK_ELEMENT, document.getDocumentElement());
        if (element  != null) {
            InputStream is = new ByteArrayInputStream(bytes);
            return ZipUtils.getUncompressedEntry(element.getAttribute(Framework.META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE), is);
        }
        return null;
    }

    public static ModelDescriptor loadMetaDescriptor(Document document)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Element element = XmlUtils.getChildElement(Framework.META_DESCRIPTOR_WORK_ELEMENT, document.getDocumentElement());
        String descriptorClass = element.getAttribute(Framework.META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE);
        return (ModelDescriptor) Class.forName(descriptorClass).getDeclaredConstructor().newInstance();
    }

    public static Version loadMetaVersion(Document document) {
        Element element = XmlUtils.getChildElement(Framework.META_VERSION_WORK_ELEMENT, document.getDocumentElement());
        if (element != null) {
            String major = element.getAttribute(Framework.META_VERSION_MAJOR_WORK_ATTRIBUTE);
            String minor = element.getAttribute(Framework.META_VERSION_MINOR_WORK_ATTRIBUTE);
            String revision = element.getAttribute(Framework.META_VERSION_REVISION_WORK_ATTRIBUTE);
            String status = element.getAttribute(Framework.META_VERSION_STATUS_WORK_ATTRIBUTE);
            return new Version(major, minor, revision, status);
        }
        return null;
    }

    public static Stamp loadMetaStamp(Document document) {
        Element element = XmlUtils.getChildElement(Framework.META_STAMP_WORK_ELEMENT, document.getDocumentElement());
        if (element != null) {
            String time = element.getAttribute(Framework.META_STAMP_TIME_WORK_ATTRIBUTE);
            String uuid = element.getAttribute(Framework.META_STAMP_UUID_WORK_ATTRIBUTE);
            if ((time != null) && (uuid != null)) {
                return new Stamp(time, uuid);
            }
        }
        return null;
    }

    public static Document loadMetaDoc(byte[] bytes)
            throws IOException, DeserialisationException, ParserConfigurationException, SAXException {

        InputStream zippedData = new ByteArrayInputStream(bytes);
        InputStream metaData = ZipUtils.getUncompressedEntry(Framework.META_WORK_ENTRY, zippedData);
        if (metaData == null) {
            throw new DeserialisationException("meta entry is missing in the work file");
        }
        Document result = XmlUtils.loadDocument(metaData);
        metaData.close();
        return result;
    }

    public static void loadSelectionState(byte[] bytes, VisualModel model, References references)
            throws IOException, ParserConfigurationException, SAXException {

        InputStream stateData = ZipUtils.getUncompressedEntry(Framework.STATE_WORK_ENTRY, new ByteArrayInputStream(bytes));
        if (stateData != null) {
            Document stateDoc = XmlUtils.loadDocument(stateData);
            Element stateElement = stateDoc.getDocumentElement();
            // Load current level
            Element levelElement = XmlUtils.getChildElement(Framework.STATE_LEVEL_WORK_ELEMENT, stateElement);
            Object currentLevel = references.getObject(levelElement.getAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE));
            if (currentLevel instanceof Container) {
                model.setCurrentLevel((Container) currentLevel);
            }
            // Load selection
            Element selectionElement = XmlUtils.getChildElement(Framework.STATE_SELECTION_WORK_ELEMENT, stateElement);
            Set<VisualNode> nodes = new HashSet<>();
            for (Element nodeElement: XmlUtils.getChildElements(Framework.COMMON_NODE_WORK_ATTRIBUTE, selectionElement)) {
                Object node = references.getObject(nodeElement.getAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE));
                if (node instanceof VisualNode) {
                    nodes.add((VisualNode) node);
                }
            }
            model.addToSelection(nodes);
        }
    }

    public static Storage loadStorage(byte[] bytes) throws IOException {
        Storage result = new Storage();
        ByteArrayInputStream zippedData = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(zippedData, StandardCharsets.UTF_8);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            String name = ze.getName();
            if (name.startsWith(Framework.STORAGE_WORK_ENTRY) && !name.equals(Framework.STORAGE_WORK_ENTRY)) {
                String key = name.substring(Framework.STORAGE_WORK_ENTRY.length());
                result.put(key, new RawData(zis));
                System.out.println("=== " + key + " ===\n" + new String(result.get(key).toByteArray()));
            }
            zis.closeEntry();
        }
        zis.close();

        return result;
    }

    public static void saveSelectionState(VisualModel visualModel, OutputStream os, ReferenceProducer visualRefs)
            throws ParserConfigurationException {

        Document stateDocument = XmlUtils.createDocument();
        Element stateRoot = stateDocument.createElement(Framework.STATE_WORK_ELEMENT);
        stateDocument.appendChild(stateRoot);
        // level
        Element levelElement = stateDocument.createElement(Framework.STATE_LEVEL_WORK_ELEMENT);
        String currentLevelRef = visualRefs.getReference(visualModel.getCurrentLevel());
        levelElement.setAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE, currentLevelRef);
        stateRoot.appendChild(levelElement);
        // selection
        Element selectionElement = stateDocument.createElement(Framework.STATE_SELECTION_WORK_ELEMENT);
        for (Node node: visualModel.getSelection()) {
            Element nodeElement = stateDocument.createElement(Framework.COMMON_NODE_WORK_ATTRIBUTE);
            String ref = visualRefs.getReference(node);
            nodeElement.setAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE, ref);
            selectionElement.appendChild(nodeElement);
        }
        stateRoot.appendChild(selectionElement);
        XmlUtils.writeDocument(stateDocument, os);
    }
    public static void saveMeta(ModelEntry modelEntry, OutputStream os, String uuid)
            throws ParserConfigurationException {

        Document metaDocument = XmlUtils.createDocument();
        Element metaRoot = metaDocument.createElement(Framework.META_WORK_ELEMENT);
        metaDocument.appendChild(metaRoot);

        Element metaVersion = metaDocument.createElement(Framework.META_VERSION_WORK_ELEMENT);
        metaVersion.setAttribute(Framework.META_VERSION_MAJOR_WORK_ATTRIBUTE, Info.getVersionMajor());
        metaVersion.setAttribute(Framework.META_VERSION_MINOR_WORK_ATTRIBUTE, Info.getVersionMinor());
        metaVersion.setAttribute(Framework.META_VERSION_REVISION_WORK_ATTRIBUTE, Info.getVersionRevision());
        metaVersion.setAttribute(Framework.META_VERSION_STATUS_WORK_ATTRIBUTE, Info.getVersionStatus());
        metaRoot.appendChild(metaVersion);

        Element metaStamp = metaDocument.createElement(Framework.META_STAMP_WORK_ELEMENT);
        Stamp stamp = modelEntry.getStamp();
        metaStamp.setAttribute(Framework.META_STAMP_TIME_WORK_ATTRIBUTE, stamp.time);
        metaStamp.setAttribute(Framework.META_STAMP_UUID_WORK_ATTRIBUTE, stamp.uuid);
        metaRoot.appendChild(metaStamp);

        Element metaDescriptor = metaDocument.createElement(Framework.META_DESCRIPTOR_WORK_ELEMENT);
        String descriptorClass = modelEntry.getDescriptor().getClass().getCanonicalName();
        metaDescriptor.setAttribute(Framework.META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE, descriptorClass);
        metaRoot.appendChild(metaDescriptor);

        Element mathElement = metaDocument.createElement(Framework.META_MATH_MODEL_WORK_ELEMENT);
        mathElement.setAttribute(Framework.META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE, Framework.MATH_MODEL_WORK_ENTRY);
        mathElement.setAttribute(Framework.META_MODEL_FORMAT_UUID_WORK_ATTRIBUTE, uuid);
        metaRoot.appendChild(mathElement);

        if (modelEntry.getVisualModel() != null) {
            Element visualElement = metaDocument.createElement(Framework.META_VISUAL_MODEL_WORK_ELEMENT);
            visualElement.setAttribute(Framework.META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE, Framework.VISUAL_MODEL_WORK_ENTRY);
            visualElement.setAttribute(Framework.META_MODEL_FORMAT_UUID_WORK_ATTRIBUTE, uuid);
            metaRoot.appendChild(visualElement);
        }
        XmlUtils.writeDocument(metaDocument, os);
    }

}
