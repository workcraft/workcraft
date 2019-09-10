package org.workcraft.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.Framework;
import org.workcraft.Version;
import org.workcraft.dom.Container;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.References;
import org.workcraft.workspace.Stamp;
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

    public static InputStream getUncompressedEntry(String name, InputStream zippedData) throws IOException {
        ZipInputStream zis = new ZipInputStream(zippedData, StandardCharsets.UTF_8);
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

    public static InputStream getMathData(byte[] bufferedInput, Document metaDoc) throws IOException {
        Element mathElement = XmlUtils.getChildElement(Framework.META_MATH_MODEL_WORK_ELEMENT, metaDoc.getDocumentElement());
        InputStream mathData = null;
        if (mathElement != null) {
            InputStream is = new ByteArrayInputStream(bufferedInput);
            mathData = getUncompressedEntry(mathElement.getAttribute(Framework.META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE), is);
        }
        return mathData;
    }

    public static InputStream getVisualData(byte[] bufferedInput, Document metaDoc) throws IOException {
        Element visualElement = XmlUtils.getChildElement(Framework.META_VISUAL_MODEL_WORK_ELEMENT, metaDoc.getDocumentElement());
        InputStream visualData = null;
        if (visualElement  != null) {
            InputStream is = new ByteArrayInputStream(bufferedInput);
            visualData = getUncompressedEntry(visualElement.getAttribute(Framework.META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE), is);
        }
        return visualData;
    }

    public static ModelDescriptor loadMetaDescriptor(Document metaDoc)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Element descriptorElement = XmlUtils.getChildElement(Framework.META_DESCRIPTOR_WORK_ELEMENT, metaDoc.getDocumentElement());
        String descriptorClass = descriptorElement.getAttribute(Framework.META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE);
        return (ModelDescriptor) Class.forName(descriptorClass).getDeclaredConstructor().newInstance();
    }

    public static Version loadMetaVersion(Document metaDoc) {
        Element versionElement = XmlUtils.getChildElement(Framework.META_VERSION_WORK_ELEMENT, metaDoc.getDocumentElement());
        if (versionElement != null) {
            String major = versionElement.getAttribute(Framework.META_VERSION_MAJOR_WORK_ATTRIBUTE);
            String minor = versionElement.getAttribute(Framework.META_VERSION_MINOR_WORK_ATTRIBUTE);
            String revision = versionElement.getAttribute(Framework.META_VERSION_REVISION_WORK_ATTRIBUTE);
            String status = versionElement.getAttribute(Framework.META_VERSION_STATUS_WORK_ATTRIBUTE);
            return new Version(major, minor, revision, status);
        }
        return null;
    }

    public static Stamp loadMetaStamp(Document metaDoc) {
        Element stampElement = XmlUtils.getChildElement(Framework.META_STAMP_WORK_ELEMENT, metaDoc.getDocumentElement());
        if (stampElement != null) {
            String time = stampElement.getAttribute(Framework.META_STAMP_TIME_WORK_ATTRIBUTE);
            String uuid = stampElement.getAttribute(Framework.META_STAMP_UUID_WORK_ATTRIBUTE);
            if ((time != null) && (uuid != null)) {
                return new Stamp(time, uuid);
            }
        }
        return null;
    }

    public static Document loadMetaDoc(byte[] bufferedInput)
            throws IOException, DeserialisationException, ParserConfigurationException, SAXException {
        ByteArrayInputStream zippedData = new ByteArrayInputStream(bufferedInput);
        InputStream metaData = getUncompressedEntry(Framework.META_WORK_ENTRY, zippedData);
        if (metaData == null) {
            throw new DeserialisationException("meta entry is missing in the work file");
        }
        Document metaDoc = XmlUtils.loadDocument(metaData);
        metaData.close();
        return metaDoc;
    }

    public static void loadVisualModelState(byte[] bi, VisualModel model, References references)
            throws IOException, ParserConfigurationException, SAXException {
        InputStream stateData = getUncompressedEntry(Framework.STATE_WORK_ENTRY, new ByteArrayInputStream(bi));
        if (stateData != null) {
            Document stateDoc = XmlUtils.loadDocument(stateData);
            Element stateElement = stateDoc.getDocumentElement();
            // level
            Element levelElement = XmlUtils.getChildElement(Framework.STATE_LEVEL_WORK_ELEMENT, stateElement);
            Object currentLevel = references.getObject(levelElement.getAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE));
            if (currentLevel instanceof Container) {
                model.setCurrentLevel((Container) currentLevel);
            }
            // selection
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

    public static void saveSelectionState(VisualModel visualModel, OutputStream os, ReferenceProducer visualRefs)
            throws ParserConfigurationException {
        Document stateDoc = XmlUtils.createDocument();
        Element stateRoot = stateDoc.createElement(Framework.STATE_WORK_ELEMENT);
        stateDoc.appendChild(stateRoot);
        // level
        Element levelElement = stateDoc.createElement(Framework.STATE_LEVEL_WORK_ELEMENT);
        levelElement.setAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE, visualRefs.getReference(visualModel.getCurrentLevel()));
        stateRoot.appendChild(levelElement);
        // selection
        Element selectionElement = stateDoc.createElement(Framework.STATE_SELECTION_WORK_ELEMENT);
        for (Node node: visualModel.getSelection()) {
            Element nodeElement = stateDoc.createElement(Framework.COMMON_NODE_WORK_ATTRIBUTE);
            nodeElement.setAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE, visualRefs.getReference(node));
            selectionElement.appendChild(nodeElement);
        }
        stateRoot.appendChild(selectionElement);
        XmlUtils.writeDocument(stateDoc, os);
    }

}
