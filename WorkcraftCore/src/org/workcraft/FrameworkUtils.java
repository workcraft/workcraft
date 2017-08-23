package org.workcraft;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.References;
import org.workcraft.util.XmlUtils;
import org.workcraft.workspace.Stamp;
import org.xml.sax.SAXException;

public class FrameworkUtils {

    static InputStream getUncompressedEntry(String name, InputStream zippedData) throws IOException {
        ZipInputStream zis = new ZipInputStream(zippedData);
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

    static InputStream getMathData(byte[] bufferedInput, Document metaDoc) throws IOException {
        Element mathElement = XmlUtils.getChildElement(Framework.META_MATH_MODEL_WORK_ELEMENT, metaDoc.getDocumentElement());
        InputStream mathData = null;
        if (mathElement != null) {
            InputStream is = new ByteArrayInputStream(bufferedInput);
            mathData = getUncompressedEntry(mathElement.getAttribute(Framework.META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE), is);
        }
        return mathData;
    }

    static InputStream getVisualData(byte[] bufferedInput, Document metaDoc) throws IOException {
        Element visualElement = XmlUtils.getChildElement(Framework.META_VISUAL_MODEL_WORK_ELEMENT, metaDoc.getDocumentElement());
        InputStream visualData = null;
        if (visualElement  != null) {
            InputStream is = new ByteArrayInputStream(bufferedInput);
            visualData = getUncompressedEntry(visualElement.getAttribute(Framework.META_MODEL_ENTRY_NAME_WORK_ATTRIBUTE), is);
        }
        return visualData;
    }

    static ModelDescriptor loadMetaDescriptor(Document metaDoc)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Element descriptorElement = XmlUtils.getChildElement(Framework.META_DESCRIPTOR_WORK_ELEMENT, metaDoc.getDocumentElement());
        String descriptorClass = XmlUtils.readStringAttr(descriptorElement, Framework.META_DESCRIPTOR_CLASS_WORK_ATTRIBUTE);
        return (ModelDescriptor) Class.forName(descriptorClass).newInstance();
    }

    static Stamp loadMetaStamp(Document metaDoc)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Stamp stamp = null;
        Element stampElement = XmlUtils.getChildElement(Framework.META_STAMP_WORK_ELEMENT, metaDoc.getDocumentElement());
        if (stampElement != null) {
            String time = XmlUtils.readStringAttr(stampElement, Framework.META_STAMP_TIME_WORK_ATTRIBUTE);
            String uuid = XmlUtils.readStringAttr(stampElement, Framework.META_STAMP_UUID_WORK_ATTRIBUTE);
            if ((time != null) && (uuid != null)) {
                stamp = new Stamp(time, uuid);
            }
        }
        return stamp;
    }

    static Version loadMetaVersion(Document metaDoc)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Version version = null;
        Element versionElement = XmlUtils.getChildElement(Framework.META_VERSION_WORK_ELEMENT, metaDoc.getDocumentElement());
        if (versionElement != null) {
            String major = XmlUtils.readStringAttr(versionElement, Framework.META_VERSION_MAJOR_WORK_ATTRIBUTE);
            String minor = XmlUtils.readStringAttr(versionElement, Framework.META_VERSION_MINOR_WORK_ATTRIBUTE);
            String revision = XmlUtils.readStringAttr(versionElement, Framework.META_VERSION_REVISION_WORK_ATTRIBUTE);
            String status = XmlUtils.readStringAttr(versionElement, Framework.META_VERSION_STATUS_WORK_ATTRIBUTE);
            version = new Version(major, minor, revision, status);
        }
        return version;
    }

    static Document loadMetaDoc(byte[] bufferedInput)
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

    static void loadVisualModelState(byte[] bi, VisualModel model, References references)
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
            Set<Node> nodes = new HashSet<>();
            for (Element nodeElement: XmlUtils.getChildElements(Framework.COMMON_NODE_WORK_ATTRIBUTE, selectionElement)) {
                Object node = references.getObject(nodeElement.getAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE));
                if (node instanceof Node) {
                    nodes.add((Node) node);
                }
            }
            model.addToSelection(nodes);
        }
    }

    static void saveSelectionState(VisualModel visualModel, OutputStream os, ReferenceProducer visualRefs)
            throws ParserConfigurationException, IOException {
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
            Element nodeElement = stateDoc.createElement("node");
            nodeElement.setAttribute(Framework.COMMON_REF_WORK_ATTRIBUTE, visualRefs.getReference(node));
            selectionElement.appendChild(nodeElement);
        }
        stateRoot.appendChild(selectionElement);
        XmlUtils.writeDocument(stateDoc, os);
    }

}
