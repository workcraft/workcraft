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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.ConstructorParametersMatcher;
import org.workcraft.util.ListMap;
import org.workcraft.util.LogUtils;
import org.workcraft.util.XmlUtil;

public class PluginManager implements PluginProvider {
    public static final String VERSION_STAMP = "d971444cbd86148695f3427118632aca";

    private ListMap <Class<?>, PluginInfo<?>> plugins = new ListMap<Class<?>, PluginInfo<?>>();

    public static class PluginInstanceHolder<T> implements PluginInfo<T> {
        private final Initialiser<? extends T> initialiser;

        public PluginInstanceHolder(Initialiser<? extends T> initialiser) {
            this.initialiser = initialiser;
        }

        T instance;

        @Override
        public T newInstance() {
            return initialiser.create();
        }

        @Override
        public T getSingleton() {
            if(instance == null) {
                instance = newInstance();
            }
            return instance;
        }
    }

    public void loadManifest() throws IOException, FormatException, PluginInstantiationException {
        File file = new File(Framework.PLUGINS_FILE_PATH);
        LogUtils.logMessageLine("Loading plugins configuration from " + file.getAbsolutePath());
        loadManifest(file);
    }

    public boolean tryLoadManifest(File file) {
        if(!file.exists()) {
            LogUtils.logMessageLine("Plugin manifest \"" + file.getAbsolutePath() + "\" does not exist, plugins will be reconfigured.");
            return false;
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        DocumentBuilder db;

        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

        Element xmlroot = doc.getDocumentElement();
        if (!xmlroot.getNodeName().equals("workcraft-plugins")) {
            LogUtils.logWarningLine("Bad plugin manifest: root tag should be 'workcraft-plugins'.");
            return false;
        }

        final Element versionElement = XmlUtil.getChildElement("version", xmlroot);

        if (versionElement == null || !XmlUtil.readStringAttr(versionElement, "value").equals(VERSION_STAMP)) {
            LogUtils.logWarningLine("Old plugin manifest version detected. Will reconfigure.");
            return false;
        }

        plugins.clear();

        for(Element pluginElement : XmlUtil.getChildElements("plugin", xmlroot)) {
            LegacyPluginInfo info = new LegacyPluginInfo(pluginElement);
            for (String interfaceName : info.getInterfaces())
            try {
                plugins.put(Class.forName(interfaceName), new PluginInstanceHolder<Object>(info));
            } catch (ClassNotFoundException e) {
                LogUtils.logErrorLine("Class '" + info.getClassName() + "' implements unknown interface '" + interfaceName +"'. Skipping interface.");
            }
        }

        return true;
    }

    public void loadManifest(File file) throws IOException, FormatException, PluginInstantiationException {
        if(!tryLoadManifest(file)) {
            reconfigure();
        } else {
            initModules();
        }
    }

    private void initModules() {
        for(PluginInfo<? extends Module> info : getPlugins(Module.class)) {
            final Module module = info.newInstance();
            try {
                LogUtils.logMessageLine("  Loading module: " + module.getDescription());
                module.init();
            }
            catch(Throwable th) {
                LogUtils.logErrorLine("Failed initialisation of module " + module.toString());
            }
        }
    }

    public static void saveManifest(List<LegacyPluginInfo> plugins) throws IOException {
        File file = new File(Framework.PLUGINS_FILE_PATH);
        LogUtils.logMessageLine("Saving plugins configuration to " + file.getAbsolutePath());
        saveManifest(file, plugins);
    }

    public static void saveManifest(File file, List<LegacyPluginInfo> plugins) throws IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc;
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.newDocument();
        } catch (ParserConfigurationException e) {
            System.err.println(e.getMessage());
            return;
        }

        Element root = doc.createElement("workcraft-plugins");
        doc.appendChild(root);
        root = doc.getDocumentElement();

        for(LegacyPluginInfo info : plugins) {
            Element e = doc.createElement("plugin");
            info.toXml(e);
            root.appendChild(e);
        }

        final Element versionElement = doc.createElement("version");
        versionElement.setAttribute("value", VERSION_STAMP);
        root.appendChild(versionElement);

        XmlUtil.saveDocument(doc, file);
    }

    private void processLegacyPlugin (Class<?> cls, LegacyPluginInfo info) throws PluginInstantiationException {
        for (String interfaceName : info.getInterfaces())
        try {
            plugins.put(Class.forName(interfaceName), new PluginInstanceHolder<Object>(info));
        } catch (ClassNotFoundException e) {
            LogUtils.logErrorLine("Class '" + info.getClassName() + "' implements unknown interface '" + interfaceName +"'. Skipping interface.");
        }
    }

    public void reconfigure() throws PluginInstantiationException {
        LogUtils.logMessageLine("Reconfiguring plugins...");
        plugins.clear();

        String[] classPathLocations = System.getProperty("java.class.path").split(System.getProperty("path.separator"));

        List<Class<?>> classes = new ArrayList<Class<?>>();
        ArrayList<LegacyPluginInfo> pluginInfos = new ArrayList<LegacyPluginInfo>();

        for (String s: classPathLocations) {
            LogUtils.logMessageLine("  Processing class path entry: " + s);
            classes.addAll(PluginFinder.search(new File(s)));
        }

        LogUtils.logMessageLine("" + classes.size() + " plugin(s) found.");

        for(Class<?> cls : classes) {
            final LegacyPluginInfo info = new LegacyPluginInfo(cls);
            pluginInfos.add(info);
            processLegacyPlugin(cls, info);
        }

        try {
            saveManifest(pluginInfos);
        } catch(IOException e) {
            System.err.println(e.getMessage());
        }

        initModules();
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<PluginInfo<? extends T>> getPlugins(Class<T> interf) {
        return (Collection<PluginInfo<? extends T>>)(Collection<?>)Collections.unmodifiableCollection(plugins.get(interf));
    }

    public <T> void registerClass(Class<T> interf, final Class<? extends T> cls) {
        registerClass(interf, new Initialiser<T>(){
            @Override
            public T create() {
                try {
                    return cls.newInstance();
                } catch (InstantiationException e) {
                    Throwable q = e;
                    System.err.println(cls.getCanonicalName());
                    while (q != null) {
                        q.printStackTrace();
                        q = q.getCause();
                    }
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public <T> void registerClass(Class<T> interf, final Class<? extends T> cls, final Object ... constructorArgs) {
        registerClass(interf, new Initialiser<T>(){
            @Override
            public T create() {
                try {
                    Class<?>[] classes = new Class<?>[constructorArgs.length];
                    for (int i=0; i<constructorArgs.length; i++) {
                        classes[i] = constructorArgs[i].getClass();
                    }
                    return new ConstructorParametersMatcher().match(cls, classes).newInstance(constructorArgs);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public <T> void registerClass(Class<T> interf, Initialiser<? extends T> initialiser) {
        if(!interf.isInterface()) {
            throw new RuntimeException("'interf' argument must be an interface");
        }
        final PluginInfo<T> pluginInfo = new PluginInstanceHolder<T>(initialiser);
        plugins.put(interf, pluginInfo);
    }

}
