package org.workcraft.plugins.balsa.stgbuilder;

import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class SignalId {
	private final Handshake owner;
	private final String name;
	public SignalId(Handshake owner, String name)
	{
		if(owner == null || name == null)
			throw new NullPointerException("Argument is null");
		this.owner = owner;
		this.name = name;
	}
	public Handshake getOwner()
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
