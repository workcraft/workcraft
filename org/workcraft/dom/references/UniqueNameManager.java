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

	public String getName(T T) {
		String name = Ts.getKey(T);
		if (name == null)
			throw new NotFoundException("Object \"" + T.toString() + "\" was not issued a name");
		return name;
	}

	public void setName(T T, String label) {
		final T occupant = Ts.getValue(label);
		if(occupant == T)
			return;
		if(occupant != null)
			throw new ArgumentException("The name \"" + label + "\" is already taken. Please choose another name.");

		if (!Identifier.isValid(label))
			throw new ArgumentException(
			"\"" + label + "\" is not a valid C-style identifier.\nThe first character must be alphabetic or an underscore and the following characters must be alphanumeric or an underscore.");

		Ts.removeValue(T);
		Ts.put(label, T);
	}

	public void setDefaultNameIfUnnamed(T T) {
		if (Ts.containsValue(T))
			return;

		String candidate;

		final String name = defaultName.eval(T);
		Integer counter = defaultNameCounters.get(name);

		if (counter == null)
			counter = 0;

		do
		{
			candidate = name + counter++;
		} while (Ts.containsKey(candidate));

		defaultNameCounters.put(candidate, counter);

		Ts.put(candidate, T);
	}

	public T get (String name) {
		T n = Ts.getValue(name);
		if (n==null)
			throw new NotFoundException ("Object \"" + name + "\" not found.");
		return n;
	}

	public void remove (T n) {
		Ts.removeValue(n);
	}
}