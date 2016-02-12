/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.annotations.DisplayName;
import org.workcraft.exceptions.FormatException;
import org.workcraft.util.XmlUtil;

public class LegacyPluginInfo implements Initialiser<Object> {
    private String displayName;
    private String className;
    private String[] interfaceNames;

    private void addInterfaces(Class<?> cls, Set<String> set) {
        if (cls == null || cls.equals(Object.class))
            return;

        for (Class<?> interf : cls.getInterfaces()) {
            set.add(interf.getName());
            addInterfaces(interf, set);
        }

        addInterfaces(cls.getSuperclass(), set);
    }

    public LegacyPluginInfo(final Class<?> cls) {
        className = cls.getName();

        DisplayName name = cls.getAnnotation(DisplayName.class);

        if(name == null)
            displayName = className.substring(className.lastIndexOf('.')+1);
        else
            displayName = name.value();

        HashSet<String> interfaces = new HashSet<String>();
        addInterfaces(cls, interfaces);
        interfaceNames = interfaces.toArray(new String[0]);
    }

    @Override
    public Object create() {
        try {
            return loadClass().getConstructor().newInstance();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public LegacyPluginInfo(Element element) throws FormatException {
        className = XmlUtil.readStringAttr(element, "class");
        if(className==null || className.isEmpty())
            throw new FormatException();

        displayName = XmlUtil.readStringAttr(element, "displayName");
        if (displayName.isEmpty())
            displayName = className.substring(className.lastIndexOf('.')+1);

        NodeList nl = element.getElementsByTagName("interface");
        interfaceNames = new String[nl.getLength()];

        for (int i=0; i<nl.getLength(); i++)
            interfaceNames[i] = ((Element) nl.item(i)).getAttribute("name");
    }

    public void toXml(Element element) {
        XmlUtil.writeStringAttr(element, "class", className);
        XmlUtil.writeStringAttr(element, "displayName", displayName);

        for (String i : interfaceNames) {
            Element e = element.getOwnerDocument().createElement("interface");
            e.setAttribute("name", i);
            element.appendChild(e);
        }
    }

    public Class<?> loadClass() throws ClassNotFoundException {
        return Class.forName(className);
    }

    public String[] getInterfaces() {
        return interfaceNames.clone();
    }

    public boolean isInterfaceImplemented(String interfaceClassName) {
        for (String s : interfaceNames)
            if (s.equals(interfaceClassName))
                return true;
        return false;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
