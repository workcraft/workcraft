package org.workcraft.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.references.AltFileReference;
import org.workcraft.dom.references.FileReference;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.builtin.serialisation.XMLModelSerialiser;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hashes of the model math layer. A hash does not depend on the model title, comments, paths of referenced files,
 * visual layer, selection state, stamp or meta data, so it changes only when the model semantics changes. The
 * referenced files are not hashed here, they are to be covered by the dependencies of verification goals.
 */
public final class ModelHasher {

    public static final String NO_FILE_HASH = "none";

    private static final String HASH_ALGORITHM = "SHA-256";

    private static final String ABSTRACT_MODEL_ELEMENT = "AbstractModel";
    private static final String PROPERTY_ELEMENT = "property";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String TITLE_PROPERTY_NAME = "title";
    private static final String NODE_ELEMENT = "node";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String PATH_ATTRIBUTE = "path";
    private static final String COMMENT_CLASS_NAME = CommentNode.class.getName();

    private ModelHasher() {
    }

    public static String getMathHash(WorkspaceEntry we) {
        return getMathHash(we.getModelEntry());
    }

    public static String getMathHash(ModelEntry me) {
        Model<?, ?> mathModel = me.getMathModel();
        if (mathModel == null) {
            return null;
        }
        try {
            XMLModelSerialiser serialiser = new XMLModelSerialiser(Framework.getInstance().getPluginManager());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            mathModel.beforeSerialisation();
            serialiser.serialise(mathModel, os, null);
            return getHash(excludeNonSemantics(os.toByteArray()));
        } catch (SerialisationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove from the serialised math model the data that does not affect verification results: the title, the
     * paths of file references and the comment nodes. The files that a result depends on are covered by the
     * dependencies of the verification goal (hashed by content), and the paths are location-specific, as they
     * are absolute for a loaded model.
     */
    private static byte[] excludeNonSemantics(byte[] bytes) throws IOException, SAXException {
        Document document = XmlUtils.loadDocument(new ByteArrayInputStream(bytes));
        Element modelElement = document.getDocumentElement();
        for (Element abstractModelElement : XmlUtils.getChildElements(ABSTRACT_MODEL_ELEMENT, modelElement)) {
            for (Element propertyElement : XmlUtils.getChildElements(PROPERTY_ELEMENT, abstractModelElement)) {
                if (TITLE_PROPERTY_NAME.equals(propertyElement.getAttribute(NAME_ATTRIBUTE))) {
                    abstractModelElement.removeChild(propertyElement);
                }
            }
        }

        // Collect the elements first, as the document is modified while they are processed
        NodeList elements = document.getElementsByTagName("*");
        List<Element> commentElements = new ArrayList<>();
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            String className = element.getAttribute(CLASS_ATTRIBUTE);
            if (NODE_ELEMENT.equals(element.getTagName()) && COMMENT_CLASS_NAME.equals(className)) {
                commentElements.add(element);
            } else if (PROPERTY_ELEMENT.equals(element.getTagName()) && isFileReferenceClass(className)) {
                removePathAttributes(element);
            }
        }
        for (Element commentElement : commentElements) {
            commentElement.getParentNode().removeChild(commentElement);
        }
        // Indentation of the removed elements must not remain in the document
        removeWhitespaceTextNodes(modelElement);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XmlUtils.writeDocument(document, os);
        return os.toByteArray();
    }

    /**
     * Remove the indentation between elements. The text of leaf elements is data, so it is left intact.
     */
    private static void removeWhitespaceTextNodes(Element parent) {
        List<Element> childElements = new ArrayList<>();
        List<Node> blankTextNodes = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element childElement) {
                childElements.add(childElement);
            } else if ((child.getNodeType() == Node.TEXT_NODE) && child.getTextContent().isBlank()) {
                blankTextNodes.add(child);
            }
        }
        if (!childElements.isEmpty()) {
            blankTextNodes.forEach(parent::removeChild);
        }
        childElements.forEach(ModelHasher::removeWhitespaceTextNodes);
    }

    private static boolean isFileReferenceClass(String className) {
        return FileReference.class.getName().equals(className) || AltFileReference.class.getName().equals(className);
    }

    private static void removePathAttributes(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = attributes.getLength() - 1; i >= 0; i--) {
            String name = attributes.item(i).getNodeName();
            if (name.startsWith(PATH_ATTRIBUTE)) {
                element.removeAttribute(name);
            }
        }
    }

    /**
     * Hash of the model in a file that a verification result depends on, e.g. the environment or a refinement of
     * a circuit. Only work files are supported (as the files used by verification commands can be assumed to be),
     * and the model is loaded in the same way as when it is used, including unsaved changes if the file is open.
     * The hash is then calculated for the model math layer, as for the verified model itself.
     *
     * @return {@link #NO_FILE_HASH} if there is no file, or null if the model cannot be read from the file
     */
    public static String getFileHash(File file) {
        if ((file == null) || !file.exists()) {
            return NO_FILE_HASH;
        }
        try {
            ModelEntry me = WorkUtils.loadModel(file);
            return (me == null) ? null : getMathHash(me);
        } catch (DeserialisationException e) {
            return null;
        }
    }

    /**
     * Hash of the concatenation of the parts, where each part is length-prefixed to avoid ambiguity.
     */
    public static String getHash(String... parts) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (String part : parts) {
            byte[] bytes = (part == null ? "" : part).getBytes(StandardCharsets.UTF_8);
            byte[] prefix = (bytes.length + ":").getBytes(StandardCharsets.UTF_8);
            os.write(prefix, 0, prefix.length);
            os.write(bytes, 0, bytes.length);
        }
        return getHash(os.toByteArray());
    }

    private static String getHash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            StringBuilder result = new StringBuilder();
            for (byte b : digest.digest(bytes)) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
