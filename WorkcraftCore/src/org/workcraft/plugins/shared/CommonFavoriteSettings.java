package org.workcraft.plugins.shared;

import org.workcraft.Config;
import org.workcraft.PluginUtils;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CommonFavoriteSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonFavoriteSettings";

    private static final String keyShowAll = prefix + ".filterFavorites";

    private static final boolean defaultShowAll = false;

    private static boolean filterFavorites = defaultShowAll;

    private static final HashMap<String, Boolean> favoriteMap = new HashMap<>();

    public CommonFavoriteSettings() {
        properties.add(new PropertyDeclaration<CommonFavoriteSettings, Boolean>(
                this, "Filter favorite model types in New work dialog",
                Boolean.class, true, false, false) {
            @Override
            protected void setter(CommonFavoriteSettings object, Boolean value) {
                setFilterFavorites(value);
            }
            @Override
            protected Boolean getter(CommonFavoriteSettings object) {
                return getFilterFavorites();
            }
        });
        for (String name: PluginUtils.getSortedModelDisplayNames()) {
            properties.add(new PropertyDeclaration<CommonFavoriteSettings, Boolean>(
                    this, "  - " + name, Boolean.class, true, false, false) {
                @Override
                protected void setter(CommonFavoriteSettings object, Boolean value) {
                    setIsFavorite(name, value);
                }
                @Override
                protected Boolean getter(CommonFavoriteSettings object) {
                    return getIsFavorite(name);
                }
            });
        }
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setFilterFavorites(config.getBoolean(keyShowAll, defaultShowAll));
        for (ModelDescriptor descriptor: PluginUtils.getModelDescriptors()) {
            String name = descriptor.getDisplayName();
            String key = getKey(name);
            boolean isFavoriteDefault = descriptor.getRating().compareTo(ModelDescriptor.Rating.NORMAL) >= 0;
            setIsFavorite(name, config.getBoolean(key, isFavoriteDefault));
        }
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyShowAll, getFilterFavorites());
        for (String name: PluginUtils.getSortedModelDisplayNames()) {
            String key = getKey(name);
            config.setBoolean(key, getIsFavorite(name));
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
        return "New work favorites";
    }

    public static boolean getFilterFavorites() {
        return filterFavorites;
    }

    public static void setFilterFavorites(boolean value) {
        filterFavorites = value;
    }

    public static boolean getIsFavorite(String name) {
        Boolean value = favoriteMap.get(name);
        return (value == null) ? false : value;
    }

    public static void setIsFavorite(String name, boolean value) {
        favoriteMap.put(name, value);
    }

}
