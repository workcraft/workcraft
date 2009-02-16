package org.workcraft.testing.plugins.petri;

import java.util.HashMap;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;

interface KeyProvider<T>
{
	public Object getKey(T item);
}

class ComponentLabelExtractor implements KeyProvider<Component>
{
	public Object getKey(Component component) {
		return component.getLabel();
	}
}

class Finder<T>
{
	private final HashMap<Object, T> map;
	private final KeyProvider<T> keyProvider;

	public Finder(Iterable<T> items, KeyProvider<T> keyProvider)
	{
		this.keyProvider = keyProvider;
		map = new HashMap<Object, T>();
		for(T item : items)
			map.put(keyProvider.getKey(item), item);
	}

	public T getMatching(T item)
	{
		return map.get(keyProvider.getKey(item));
	}
}

class ConnectionByComponentsIdentifier implements
		KeyProvider<Connection> {

	private final KeyProvider<Component> componentKeyProvider;

	class Pair
	{
		private final Object o1;
		private final Object o2;

		public Pair(Object o1, Object o2)
		{
			this.o1 = o1;
			this.o2 = o2;
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 37 * result + o1.hashCode();
			result = 37 * result + o2.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			Pair other = (Pair)obj;
			return o1.equals(other.o1) && o2.equals(other.o2);
		}
	}

	public ConnectionByComponentsIdentifier(
			KeyProvider<Component> componentKeyProvider) {
				this.componentKeyProvider = componentKeyProvider;
	}

	public Object getKey(Connection item) {

		return new Pair(componentKeyProvider.getKey(item.getFirst()),
				componentKeyProvider.getKey(item.getSecond()));
	}
}
