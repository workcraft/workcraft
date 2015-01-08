package org.workcraft.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnumUtils {

	static public <T extends Enum<T>> Map<T, String> getChoice(Class<T> type) {
		LinkedHashMap<T, String> choice = new LinkedHashMap<T, String>();
		if (type.isEnum()) {
			for (T item : type.getEnumConstants()) {
				choice.put(item, item.toString());
			}
		}
		return choice;
	}

}
