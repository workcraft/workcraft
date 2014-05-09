package org.workcraft.dom.references;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.util.Func;
import org.workcraft.util.Identifier;
import org.workcraft.util.TwoWayMap;

public class UniqueNameManager<T> implements NameManager<T> {
	private Func<T, String> nodePrefix;
	private Map<String, Integer> prefixCount = new HashMap<String, Integer>();
	private TwoWayMap<String, T> Ts = new TwoWayMap<String, T>();

	public UniqueNameManager() {
		this(null);
	}

	public UniqueNameManager(Func<T, String> nodePrefix) {
		if (nodePrefix != null)
			this.nodePrefix = nodePrefix;
		else
			this.nodePrefix = new Func<T, String>() {
			@Override
			public String eval(T arg) {
				return "unnamed";
			}
		};
	}

	public String getNodePrefix(T t) {
		return nodePrefix.eval(t);
	}

	public Integer getPrefixCount(String prefix) {
		if (prefixCount.containsKey(prefix)) {
			return prefixCount.get(prefix);
		} else {
			return 0;
		}
	}

	public Integer setPrefixCount(String prefix, Integer count) {
		return prefixCount.put(prefix, count);
	}

	public String getNameQuiet(T t) {
		String name = Ts.getKey(t);
		return name;
	}

	public boolean isNamed(T t) {
		String name = getNameQuiet(t);
		if (name == null) {
			return false;
		}
		return true;
	}


	public String getName(T t) {
		String name = getNameQuiet(t);
		if (name == null) {
			throw new NotFoundException("Object \"" + t.toString() + "\" was not issued a name");
		}
		return name;
	}

	public void setName(T t, String name) {
		final T occupant = Ts.getValue(name);
		if(occupant == t) {
			return;
		}
		if(occupant != null) {
			throw new ArgumentException("The name \"" + name + "\" is already taken. Please choose another name.");
		}
		if (!Identifier.isValid(name)) {
			throw new ArgumentException("\"" + name + "\" is not a valid C-style identifier.\n"
					+ "The first character must be alphabetic or an underscore and the following characters must be alphanumeric or an underscore.");
		}
		Ts.removeValue(t);
		Ts.put(name, t);
	}

	public void setDefaultNameIfUnnamed(T t) {
		if (Ts.containsValue(t)) {
			return;
		}
		final String prefix = getNodePrefix(t);
		Integer count = getPrefixCount(prefix);
		String name;
		do	{
			name = prefix + count++;
		} while (Ts.containsKey(name));
		setPrefixCount(prefix, count);
		Ts.put(name, t);
	}

	public T get (String name) {
		return Ts.getValue(name);
	}

	public void remove (T t) {
		Ts.removeValue(t);
	}

}