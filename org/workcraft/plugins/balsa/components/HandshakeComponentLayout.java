package org.workcraft.plugins.balsa.components;

import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public interface HandshakeComponentLayout {
	public Handshake getTop();
	public Handshake getBottom();
	public Handshake[][] getLeft();
	public Handshake[][] getRight();
}
