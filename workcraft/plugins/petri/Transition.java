package org.workcraft.plugins.petri;

import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;

@DisplayName("Transition")
@VisualClass("org.workcraft.plugins.petri.VisualTransition")
public class Transition extends Component {
	public boolean isEnabled() {
		for (Component p : getPreset())
				if (((Place)p).getTokens() < 1)
					return false;
		return true;
	}

	public void fire() {
		for (Component c : getPreset()) {
			Place p = (Place)c;
			p.setTokens(p.getTokens() - 1);
		}

		for (Component c : getPostset()) {
			Place p = (Place)c;
			p.setTokens(p.getTokens() + 1);
		}
	}
}
