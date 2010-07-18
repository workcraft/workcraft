package org.workcraft.util;

import java.util.HashSet;
import java.util.Set;

public class SetUtils {
	public static <T> Set<T> intersection (Set<T> set1, Set<T> set2) {
		Set<T> result = new HashSet<T>();
		for (T o : set1)
			if (set2.contains(o))
				result.add(o);
		return result;
	}

	public static <T> Set<T> union (Set<T> set1, Set<T> set2) {
		Set<T> result = new HashSet<T>(set1);
		result.addAll(set2);
		return result;
	}
}
