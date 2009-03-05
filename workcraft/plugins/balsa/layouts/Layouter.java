package org.workcraft.plugins.balsa.layouts;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public abstract class Layouter <T extends Component> {
	abstract HandshakeComponentLayout getLayout(T component, Map<String, Handshake> handshakes);

	@SuppressWarnings("unchecked")
	HandshakeComponentLayout getComponentLayout(Component component, Map<String, Handshake> handshakes)
	{
		return getLayout((T)component, handshakes);
	}
}
