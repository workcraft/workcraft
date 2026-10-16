package org.workcraft.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
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

/**
 * Hashes of the model math layer. A hash does not depend on the model title, visual layer, selection state,
 * stamp or meta data, so it changes only when the model semantics changes.
 */
public final class ModelHasher {

    public static final String NO_FILE_HASH = "none";

    private static final String HASH_ALGORITHM = "SHA-256";

    private static final String ABSTRACT_MODEL_ELEMENT = "AbstractModel";
    private static final String PROPERTY_ELEMENT = "property";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String TITLE_PROPERTY_NAME = "title";

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
            return getHash(excludeTitle(os.toByteArray()));
        } catch (SerialisationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Model title does not affect verification results, so it is removed from the serialised math model.
     */
    private static byte[] excludeTitle(byte[] bytes) throws IOException, SAXException {
        Document document = XmlUtils.loadDocument(new ByteArrayInputStream(bytes));
        Element modelElement = document.getDocumentElement();
        for (Element abstractModelElement : XmlUtils.getChildElements(ABSTRACT_MODEL_ELEMENT, modelElement)) {
            for (Element propertyElement : XmlUtils.getChildElements(PROPERTY_ELEMENT, abstractModelElement)) {
                if (TITLE_PROPERTY_NAME.equals(propertyElement.getAttribute(NAME_ATTRIBUTE))) {
                    abstractModelElement.removeChild(propertyElement);
                }
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XmlUtils.writeDocument(document, os);
        return os.toByteArray();
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
