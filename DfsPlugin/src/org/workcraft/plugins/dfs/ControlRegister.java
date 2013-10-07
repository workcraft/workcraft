package org.workcraft.plugins.dfs;

import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualControlRegister.class)
public class ControlRegister extends BinaryRegister {

	public enum SynchronisationType {
		PLAIN("plain"),
		AND("and"),
		OR("or");

		private final String name;

		private SynchronisationType(String name) {
			this.name = name;
		}

		static public Map<String, SynchronisationType> getChoice() {
			LinkedHashMap<String, SynchronisationType> choice = new LinkedHashMap<String, SynchronisationType>();
			for (SynchronisationType item : SynchronisationType.values()) {
				choice.put(item.name, item);
			}
			return choice;
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
