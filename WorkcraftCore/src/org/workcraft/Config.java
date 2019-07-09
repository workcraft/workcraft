package org.workcraft;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;

public class Config {

    private final HashMap<String, HashMap<String, String>> groups = new HashMap<>();
    private final HashMap<String, String> rootGroup = new HashMap<>();

    public String get(String key) {
        String[] k = key.split("\\.", 2);
        HashMap<String, String> group;

        if (k.length == 1) {
            return rootGroup.get(k[0]);
        } else {
            group = groups.get(k[0]);
            if (group == null) {
                return null;
            }
            return group.get(k[1]);
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
        String s = get(key);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
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
        String s = get(key);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public void setDouble(String key, double value) {
        set(key, toString(value));
    }

    public static String toString(double value) {
        return Double.toString(value);
    }

    public double getDouble(String key, double defaultValue) {
        String s = get(key);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public void setColor(String key, Color value) {
        if (value != null) {
            set(key, toString(value));
        }
    }

    public static String toString(Color value) {
        return String.format("#%x", value.getRGB() & 0xffffff);
    }

    public Color getColor(String key, Color defaultValue) {
        String s = get(key);
        if (s == null || s.charAt(0) != '#') {
            return defaultValue;
        }
        try {
            return new Color(Integer.parseInt(s.substring(1), 16), false);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public void setBoolean(String key, boolean value) {
        set(key, toString(value));
    }

    public static String toString(boolean value) {
        return Boolean.toString(value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String s = get(key);
        if (s == null) {
            return defaultValue;
        } else {
            return Boolean.parseBoolean(s);
        }
    }

    public void set(String key, String value) {
        String[] k = key.split("\\.", 2);
        HashMap<String, String> group;
        if (k.length == 1) {
            rootGroup.put(k[0], value);
        } else {
            group = groups.get(k[0]);
            if (group == null) {
                group = new HashMap<>();
                groups.put(k[0], group);
            }
            group.put(k[1], value);
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

            if (element.getTagName().equals("var")) {
                set(element.getAttribute("name"), element.getAttribute("value"));
            } else {
                if (element.getTagName().equals("group")) {
                    String name = element.getAttribute("name");
                    // FIXME: Skipping deprecated gui group (now split into window, toolbar, and recent)
                    if ("gui".equals(name)) {
                        continue;
                    }
                    NodeList groupNodes = element.getChildNodes();
                    for (int j = 0; j < groupNodes.getLength(); j++) {
                        if (!(groupNodes.item(j) instanceof Element)) {
                            continue;
                        }
                        Element e2 = (Element) groupNodes.item(j);
                        if (e2.getTagName().equals("var")) {
                            set(name + "." + e2.getAttribute("name"), e2.getAttribute("value"));
                        }
                    }
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

        Element root = doc.createElement("workcraft-config");
        doc.appendChild(root);

        for (String k: rootGroup.keySet()) {
            Element var = doc.createElement("var");
            var.setAttribute("name", k);
            var.setAttribute("value", rootGroup.get(k));
            root.appendChild(var);
        }

        for (String k: groups.keySet()) {
            Element group = doc.createElement("group");
            group.setAttribute("name", k);

            HashMap<String, String> g = groups.get(k);
            for (String l: g.keySet()) {
                Element var = doc.createElement("var");
                var.setAttribute("name", l);
                var.setAttribute("value", g.get(l));
                group.appendChild(var);
            }
            root.appendChild(group);
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");

            File parentDir = file.getParentFile();
            if ((parentDir != null) && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            DOMSource source = new DOMSource(doc);
            CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
            StreamResult result = new StreamResult(new OutputStreamWriter(fos, utf8Encoder));

            transformer.transform(source, result);
            fos.close();
        } catch (TransformerException | IOException e) {
            e.printStackTrace();
        }
    }

}
