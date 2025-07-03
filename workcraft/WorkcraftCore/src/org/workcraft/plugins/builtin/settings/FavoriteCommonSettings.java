package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.PluginManager;

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
        // This is because plugins need to be loaded first, therefore static block cannot be used.
        if (properties.isEmpty()) {
            properties.add(new PropertyDeclaration<>(Boolean.class,
                    "Filter favorite model types in New work dialog",
                    FavoriteCommonSettings::setFilterFavorites,
                    FavoriteCommonSettings::getFilterFavorites));


            PluginManager pm = Framework.getInstance().getPluginManager();
            List<ModelDescriptor> sortedModelDescriptors = pm.getSortedModelDescriptors();
            if (!sortedModelDescriptors.isEmpty()) {
                properties.add(PropertyHelper.createSeparatorProperty("Favorite model types"));

                for (ModelDescriptor descriptor : sortedModelDescriptors) {
                    String displayName = descriptor.getDisplayName();
                    properties.add(new PropertyDeclaration<>(Boolean.class,
                            PropertyHelper.indentWithBullet(displayName),
                            value -> setIsFavorite(displayName, value),
                            () -> getIsFavorite(displayName)));
                }
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
        PluginManager pm = Framework.getInstance().getPluginManager();
        for (ModelDescriptor descriptor : pm.getSortedModelDescriptors()) {
            String displayName = descriptor.getDisplayName();
            String key = getKey(displayName);
            boolean isFavoriteDefault = descriptor.getRating().compareTo(ModelDescriptor.Rating.NORMAL) >= 0;
            setIsFavorite(displayName, config.getBoolean(key, isFavoriteDefault));
        }
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyShowAll, getFilterFavorites());
        PluginManager pm = Framework.getInstance().getPluginManager();
        for (ModelDescriptor descriptor : pm.getSortedModelDescriptors()) {
            String displayName = descriptor.getDisplayName();
            String key = getKey(displayName);
            config.setBoolean(key, getIsFavorite(displayName));
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
        return (value != null) && value;
    }

    public static void setIsFavorite(String name, boolean value) {
        favoriteMap.put(name, value);
    }

}
