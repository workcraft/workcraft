package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.utils.PluginUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FavoriteCommonSettings extends AbstractCommonSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonFavoriteSettings";

    private static final String keyShowAll = prefix + ".filterFavorites";

    private static final boolean defaultShowAll = false;

    private static boolean filterFavorites = defaultShowAll;

    private static final HashMap<String, Boolean> favoriteMap = new HashMap<>();

    public FavoriteCommonSettings() {
        // Property initialisation must be in constructor, and only once.
        // This is because plugins need to be loaded first, therefor static block cannot be used.
        if (properties.isEmpty()) {
            properties.add(new PropertyDeclaration<>(Boolean.class,
                    "Filter favorite model types in New work dialog",
                    FavoriteCommonSettings::setFilterFavorites,
                    FavoriteCommonSettings::getFilterFavorites));

            for (String name : PluginUtils.getSortedModelDisplayNames()) {
                properties.add(new PropertyDeclaration<>(Boolean.class,
                        "  - " + name,
                        (value) -> setIsFavorite(name, value),
                        () -> getIsFavorite(name)));
            }
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
