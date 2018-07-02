package org.workcraft.plugins.shared;

import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.PluginInfo;

import java.util.*;

public class CommonPluginSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonPluginSettings";

    private static final HashMap<String, Boolean> map = new HashMap<>();

    public CommonPluginSettings() {
        for (String name: getModelNames()) {
            properties.add(new PropertyDeclaration<CommonPluginSettings, Boolean>(
                    this, name, Boolean.class, true, false, false) {
                @Override
                protected void setter(CommonPluginSettings object, Boolean value) {
                    set(name, value);
                }
                @Override
                protected Boolean getter(CommonPluginSettings object) {
                    return get(name);
                }
            });
        }
    }

    private List<String> getModelNames() {
        List<String> result = new ArrayList<>();
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        final Collection<PluginInfo<? extends ModelDescriptor>> plugins = pm.getPlugins(ModelDescriptor.class);
        for (PluginInfo<? extends ModelDescriptor> plugin : plugins) {
            String displayName = plugin.getSingleton().getDisplayName();
            if ((displayName != null) && !displayName.isEmpty()) {
                result.add(displayName);
            }
        }
        return result;
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        for (String name: getModelNames()) {
            String key = getKey(name);
            set(name, config.getBoolean(key, true));
        }
    }

    @Override
    public void save(Config config) {
        for (String name: getModelNames()) {
            String key = getKey(name);
            config.setBoolean(key, get(name));
        }
    }

    private String getKey(String name) {
        return prefix + "." + name;
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Plugins";
    }

    public static boolean get(String name) {
        Boolean value = map.get(name);
        return value == null ? false : true;
    }

    public static void set(String name, boolean value) {
        map.put(name, value);
    }

}
