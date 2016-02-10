package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.plugins.stg.STGSettings;
import org.workcraft.util.ColorUtils;

public class CoreDensityMap {
    final static private float DENSITY_MAP_BRIGHTNESS_TOP = 0.5f;
    final static private float DENSITY_MAP_BRIGHTNESS_BOTTOM = 1.0f;

    private final HashMap<String, Integer> nameToDensity;
    private final HashMap<Integer, Color> densityToColor;
    private final ArrayList<Integer> densityPalette;
    private final boolean isReduced;

    public CoreDensityMap(Collection<Core> cores) {
        nameToDensity = buildNameToDensityMap(cores);
        densityToColor = buildDensityToColorMap(nameToDensity);
        densityPalette = buidDensityPalette(densityToColor.keySet());
        isReduced = getPaletteSize() < densityToColor.size();
    }

    private HashMap<String, Integer> buildNameToDensityMap(Collection<Core> cores) {
        HashMap<String, Integer> result = new HashMap<>();
        for (Core core: cores) {
            for (String name: core) {
                int density = result.containsKey(name) ? result.get(name) : 0;
                density++;
                result.put(name, density);
            }
        }
        return result;
    }

    private HashMap<Integer, Color> buildDensityToColorMap(HashMap<String, Integer> densityMap) {
        HashMap<Integer, Color> result = new HashMap<>();
        HashSet<Integer> densitySet = new HashSet<>(densityMap.values());
        ArrayList<Integer> densityPalette = buidDensityPalette(densitySet);
        HashMap<Integer, Integer> densityToLevel = new HashMap<>();
        for (int level = 0; level < densityPalette.size(); level++) {
            int density = densityPalette.get(level);
            densityToLevel.put(density, level);
        }
        int levelLimit = STGSettings.getDensityMapLevelLimit();
        int levelCount = (densitySet.size() < levelLimit) ? densitySet.size() : levelLimit;
        float[] bs = getBrightnessLevels(levelCount);
        Color[] palette = ColorUtils.getHsbPalette(new float[]{0.05f}, new float[]{0.4f}, bs);
        for (String name: densityMap.keySet()) {
            int density = densityMap.get(name);
            int level = 0;
            if (densityToLevel.containsKey(density)) {
                level = densityToLevel.get(density);
            }
            Color color = palette[level];
            result.put(density, color);
        }
        return result;
    }

    private float[] getBrightnessLevels(int densityMapGradeCount) {
        float[] result = new float[densityMapGradeCount];
        if (densityMapGradeCount == 1) {
            result[0] = DENSITY_MAP_BRIGHTNESS_TOP;
        } else {
            float bDelta = (DENSITY_MAP_BRIGHTNESS_TOP - DENSITY_MAP_BRIGHTNESS_BOTTOM) / (densityMapGradeCount - 1);
            for (int i = 0; i < densityMapGradeCount; i++) {
                result[i] = DENSITY_MAP_BRIGHTNESS_BOTTOM + i * bDelta;
            }
        }
        return result;
    }

    private ArrayList<Integer> buidDensityPalette(Set<Integer> densitySet) {
        ArrayList<Integer> densityList = new ArrayList<>(densitySet);
        Collections.sort(densityList);
        int levelLimit = STGSettings.getDensityMapLevelLimit();
        int fromIndex = (densityList.size() < levelLimit) ? 0 : densityList.size() - levelLimit;
        return new ArrayList<>(densityList.subList(fromIndex, densityList.size()));
    }

    public boolean isReduced() {
        return isReduced;
    }

    public int getPaletteSize() {
        return densityPalette.size();
    }

    public int getDensity(String name) {
        int result = 0;
        if (nameToDensity.containsKey(name)) {
            result = nameToDensity.get(name);
        }
        return result;
    }

    public Color getColor(String name) {
        int density = getDensity(name);
        return densityToColor.get(density);
    }

    public Color getLevelColor(int level) {
        int density = getLevelDensity(level);
        return densityToColor.get(density);
    }

    public int getLevelDensity(int level) {
        return densityPalette.get(level);
    }

}
