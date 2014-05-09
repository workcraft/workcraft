package org.workcraft.dom.references;

public interface NameManager<T> {
	public boolean isNamed(T t);
	public String getName(T t);
	public void setName(T t, String name);
	public void setDefaultNameIfUnnamed(T t);
	public T get (String name);
	public void remove (T t);
}
