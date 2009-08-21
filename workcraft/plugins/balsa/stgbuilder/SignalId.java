package org.workcraft.plugins.balsa.stgbuilder;

public class SignalId {
	private final Object owner;
	private final String name;
	public SignalId(Object owner, String name)
	{
		if(owner == null || name == null)
			throw new NullPointerException("Argument is null");
		this.owner = owner;
		this.name = name;
	}
	public Object getOwner()
	{
		return owner;
	}
	public String getName()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return owner.hashCode() * 31 + name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SignalId))
			return false;
		SignalId other = (SignalId)obj;

		return
			owner.equals(other.owner) &&
			name.equals(other.name);
	}
}
