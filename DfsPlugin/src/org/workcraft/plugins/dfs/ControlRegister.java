package org.workcraft.plugins.dfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualControlRegister.class)
public class ControlRegister extends BinaryRegister {

	public enum SynchronisationType {
		PLAIN("plain"),
		AND("and"),
		OR("or");

		public final String name;

		private SynchronisationType(String name) {
			this.name = name;
		}
	}

	private SynchronisationType synchronisationType = SynchronisationType.PLAIN;

	public SynchronisationType getSynchronisationType() {
		return synchronisationType;
	}

	public void setSynchronisationType(SynchronisationType value) {
		this.synchronisationType = value;
		sendNotification(new PropertyChangedEvent(this, "synchronisation type"));
	}

}
