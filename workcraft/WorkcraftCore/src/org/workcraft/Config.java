package org.workcraft;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.ParseUtils;
import org.workcraft.utils.SortUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Config {

    private static final String KEY_SEPARATOR = ".";
    private static final String WORKCRAFT_CONFIG_ELEMENT_NAME = "workcraft-config";
    private static final String GROUP_ELEMENT_NAME = "group";
    private static final String VAR_ELEMENT_NAME = "var";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String VALUE_ATTRIBUTE_NAME = "value";

    private final HashMap<String, String> rootGroup = new HashMap<>();
    private final HashMap<String, HashMap<String, String>> groups = new HashMap<>();

    public void clear() {
        rootGroup.clear();
        groups.clear();
    }

    public Set<String> getGroupNames() {
        return new HashSet<>(groups.keySet());
    }

    public Set<String> getKeyNames(String groupName) {
        return new HashSet<>((isRootGroup(groupName) ? rootGroup : groups.getOrDefault(groupName, new HashMap<>())).keySet());
    }

    public String get(String key) {
        String[] splitKey = key.split(Pattern.quote(KEY_SEPARATOR), 2);
        HashMap<String, String> group;
        if (splitKey.length <= 1) {
            return rootGroup.get(key);
        } else {
            group = groups.get(splitKey[0]);
            return group == null ? null : group.get(splitKey[1]);
        }
    }

    public static String toString(String value) {
        return value;
    }

    public String getString(String key, String defaultValue) {
        String s = get(key);
        if (s == null) {
            return defaultValue;
        } else {
            return s;
        }
    }

    public void setInt(String key, int value) {
        set(key, toString(value));
    }

    public static String toString(int value) {
        return Integer.toString(value);
    }

    public int getInt(String key, int defaultValue) {
        return ParseUtils.parseInt(get(key), defaultValue);
    }

    public <T extends Enum<T>> void setEnum(String key, T value) {
        if (value != null) {
            set(key, toString(value));
        }
    }

    public static <T extends Enum<T>> String toString(T value) {
        return value.name();
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumType, T defaultValue) {
        return ParseUtils.parseEnum(get(key), enumType, defaultValue);
    }

    public void setDouble(String key, double value) {
        set(key, toString(value));
    }

    public static String toString(double value) {
        return Double.toString(value);
    }

    public double getDouble(String key, double defaultValue) {
        return ParseUtils.parseDouble(get(key), defaultValue);
    }

    public void setColor(String key, Color value) {
        if (value != null) {
            set(key, toString(value));
        }
    }

    public static String toString(Color value) {
        return ColorUtils.isOpaque(value) ? ColorUtils.getHexRGB(value) : ColorUtils.getHexARGB(value);
    }

    public Color getColor(String key, Color defaultValue) {
        return ParseUtils.parseColor(get(key), defaultValue);
    }

    public void setBoolean(String key, boolean value) {
        set(key, toString(value));
    }

    public static String toString(boolean value) {
        return Boolean.toString(value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return ParseUtils.parseBoolean(get(key), defaultValue);
    }

    public void set(String key, String value) {
        String[] splitKey = key.split(Pattern.quote("."), 2);
        HashMap<String, String> group;
        if (splitKey.length <= 1) {
            rootGroup.put(key, value);
        } else {
            group = groups.computeIfAbsent(splitKey[0], s -> new HashMap<>());
            group.put(splitKey[1], value);
        }
    }

    public void load(File file) {
        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            return;
        }

        Element root = doc.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i) instanceof Element)) {
                continue;
            }
            Element element = (Element) nodes.item(i);
            if (!GROUP_ELEMENT_NAME.equals(element.getTagName())) {
                loadVarElement(element, null);
            } else {
                String groupName = element.getAttribute(NAME_ATTRIBUTE_NAME);
                NodeList groupNodes = element.getChildNodes();
                for (int j = 0; j < groupNodes.getLength(); j++) {
                    if (!(groupNodes.item(j) instanceof Element)) {
                        continue;
                    }
                    Element childElement = (Element) groupNodes.item(j);
                    loadVarElement(childElement, groupName);
                }
            }
        }
    }

    public void save(File file) {
        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return;
        }

        Element root = doc.createElement(WORKCRAFT_CONFIG_ELEMENT_NAME);
        doc.appendChild(root);
        for (String name : rootGroup.keySet()) {
            saveVarElement(doc, root, name, rootGroup.get(name));
        }

        List<String> orderedGroupNames = SortUtils.getSortedNatural(groups.keySet());
        for (String groupName : orderedGroupNames) {
            HashMap<String, String> vars = groups.get(groupName);
            if (vars != null) {
                Element group = doc.createElement(GROUP_ELEMENT_NAME);
                group.setAttribute(NAME_ATTRIBUTE_NAME, groupName);
                List<String> orderedVarNames = SortUtils.getSortedNatural(vars.keySet());
                for (String name : orderedVarNames) {
                    saveVarElement(doc, group, name, vars.get(name));
                }
                root.appendChild(group);
            }
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");

            File parentDir = file.getParentFile();
            if ((parentDir != null) && !parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Cannot create parent directory for config file " + file.getAbsolutePath());
            }
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Cannot create config file " + file.getAbsolutePath());
            }
            FileOutputStream fos = new FileOutputStream(file);
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new OutputStreamWriter(fos, StandardCharsets.UTF_8));

            transformer.transform(source, result);
            fos.close();
        } catch (TransformerException | IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isRootGroup(String groupName) {
        return (groupName == null) || groupName.isEmpty();
    }

    private void loadVarElement(Element element, String groupName) {
        if (VAR_ELEMENT_NAME.equals(element.getTagName())) {
            String prefix = isRootGroup(groupName) ? "" : (groupName + KEY_SEPARATOR);
            set(prefix + element.getAttribute(NAME_ATTRIBUTE_NAME), element.getAttribute(VALUE_ATTRIBUTE_NAME));
        }
    }

    private void saveVarElement(Document doc, Element parent, String name, String value) {
        if ((name != null) && (value != null)) {
            Element element = doc.createElement(VAR_ELEMENT_NAME);
            element.setAttribute(NAME_ATTRIBUTE_NAME, name);
            element.setAttribute(VALUE_ATTRIBUTE_NAME, value);
            parent.appendChild(element);
        }
    }

}
