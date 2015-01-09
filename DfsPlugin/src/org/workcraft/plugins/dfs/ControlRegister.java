package org.workcraft.plugins.dfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualControlRegister.class)
public class ControlRegister extends BinaryRegister {

	private SynchronisationType synchronisationType = SynchronisationType.PLAIN;
	private double probability = 1.0;

	public enum SynchronisationType {
		PLAIN("plain"),
		AND("and"),
		OR("or");

		private final String name;

		private SynchronisationType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public SynchronisationType getSynchronisationType() {
		return synchronisationType;
	}

	public void setSynchronisationType(SynchronisationType value) {
		this.synchronisationType = value;
		sendNotification(new PropertyChangedEvent(this, "synchronisation type"));
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double value) {
		this.probability = value;
		sendNotification(new PropertyChangedEvent(this, "probability"));
	}

}
