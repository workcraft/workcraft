package org.workcraft.plugins.balsa.stg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class HandshakeExtractor {
	public static List<Handshake> extract(Map<String, Handshake> handshakes, String arrayedPortName, int size)
	{
		ArrayList<Handshake> result = new ArrayList<Handshake>();
		for(int i=0;i<size;i++)
			result.add(handshakes.get(arrayedPortName + i));
		return result;
	}

	public static Handshake extract(Map<String, Handshake> handshakes, String portName) {
		return handshakes.get(portName);
	}
}
