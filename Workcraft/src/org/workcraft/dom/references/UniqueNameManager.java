package org.workcraft.dom.references;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.util.Func;
import org.workcraft.util.Identifier;
import org.workcraft.util.TwoWayMap;

public class UniqueNameManager<T> {
	private Func<T, String> defaultName;
	private Map<String, Integer> defaultNameCounters = new HashMap<String, Integer>();
	private TwoWayMap<String, T> Ts = new TwoWayMap<String, T>();

	public UniqueNameManager() {
		this(null);
	}

	public UniqueNameManager(Func<T, String> defaultName) {
		if (defaultName != null)
			this.defaultName = defaultName;
		else
			this.defaultName = new Func<T, String>() {
			@Override
			public String eval(T arg) {
				return "unnamed";
			}
		};
	}

	public String getName(T t) {
		String name = Ts.getKey(t);
		if (name == null)
			throw new NotFoundException("Object \"" + t.toString() + "\" was not issued a name");
		return name;
	}

	public void setName(T t, String name) {
		final T occupant = Ts.getValue(name);
		if(occupant == t)
			return;
		if(occupant != null)
			throw new ArgumentException("The name \"" + name + "\" is already taken. Please choose another name.");

		if (!Identifier.isValid(name))
			throw new ArgumentException("\"" + name + "\" is not a valid C-style identifier.\nThe first character must be alphabetic or an underscore and the following characters must be alphanumeric or an underscore.");

		Ts.removeValue(t);
		Ts.put(name, t);
	}

	public void setDefaultNameIfUnnamed(T t) {
		if (Ts.containsValue(t))
			return;

		String candidate;

		final String name = defaultName.eval(t);
		Integer counter = defaultNameCounters.get(name);

		if (counter == null)
			counter = 0;

		do	{
			candidate = name + counter++;
		} while (Ts.containsKey(candidate));

		defaultNameCounters.put(name, counter);

		Ts.put(candidate, t);
	}

	public T get (String name) {
		return Ts.getValue(name);
	}

	public void remove (T t) {
		Ts.removeValue(t);
	}
}