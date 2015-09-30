package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;

import org.workcraft.util.ColorUtils;

public class Heightmap {
	final static private float HEIGHTMAP_BRIGHTNESS_TOP = 1.0f;
	final static private float HEIGHTMAP_BRIGHTNESS_BOTTOM = 0.5f;
	final static private int HEIGHTMAP_RANGE_LIMIT = 5;

	private final HashMap<String, Integer> heightmap;
	private final Color[] palette;
	private int minHeight;
	private int maxHeight;
	private boolean reduced;

	public Heightmap(Collection<Core> cores) {
		this.heightmap = new HashMap<>();
		for (Core core: cores) {
			for (String name: core) {
				int height = (heightmap.containsKey(name) ? heightmap.get(name) : 0);
				height++;
				heightmap.put(name, height);
			}
		}

		minHeight = cores.size();
		maxHeight = 0;
		for (int height: heightmap.values()) {
			if (height < minHeight) {
				minHeight = height;
			}
			if (height > maxHeight) {
				maxHeight = height;
			}
		}
		reduced = false;
		int heightmapRange = (maxHeight - minHeight + 1);
		if (heightmapRange > HEIGHTMAP_RANGE_LIMIT) {
			heightmapRange = HEIGHTMAP_RANGE_LIMIT;
			minHeight = maxHeight - HEIGHTMAP_RANGE_LIMIT + 1;
			reduced = true;
		}
		float[] bs = getBrightnessLevels(heightmapRange);
		palette = ColorUtils.getHsbPalette(new float[]{0.05f}, new float[]{0.4f}, bs);

	}

	private float[] getBrightnessLevels(int heightmapGradeCount) {
		float[] result = new float[heightmapGradeCount];
		if (heightmapGradeCount == 1) {
			result[0] = HEIGHTMAP_BRIGHTNESS_TOP;
		} else {
			float bDelta = (HEIGHTMAP_BRIGHTNESS_TOP - HEIGHTMAP_BRIGHTNESS_BOTTOM) / (heightmapGradeCount - 1);
			for (int i = 0; i < heightmapGradeCount; i++) {
				result[i] = HEIGHTMAP_BRIGHTNESS_BOTTOM + i * bDelta;
			}
		}
		return result;
	}

	public int getMinHeight() {
		return minHeight;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public boolean isReduced() {
		return reduced;
	}

	public Color getColor(int height) {
		int idx = height - getMinHeight();
		if (idx < 0) {
			idx = 0;
		}
		return palette[idx];
	}


	public Color getColor(String name) {
		Color result = null;
		if ((heightmap != null) && heightmap.containsKey(name)) {
			int height = heightmap.get(name);
			result = getColor(height);
		}
		return result;
	}

	public int getPaletteSize() {
		return ((palette == null) ? 0 : palette.length);
	}


}
