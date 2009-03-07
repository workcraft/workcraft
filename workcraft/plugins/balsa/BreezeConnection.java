package org.workcraft.plugins.balsa;

import org.workcraft.dom.Connection;

public class BreezeConnection extends Connection {

	private String handshake1;
	private String handshake2;

	public BreezeConnection(BreezeComponent first, BreezeComponent second) {
		super(first, second);
	}

	public void setHandshake1(String handshake1) {
		this.handshake1 = handshake1;
	}

	public String getHandshake1() {
		return handshake1;
	}

	public void setHandshake2(String handshake2) {
		this.handshake2 = handshake2;
	}

	public String getHandshake2() {
		return handshake2;
	}

}
