package org.workcraft.plugins.dfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualControlRegister.class)
public class ControlRegister extends MathNode {

	public enum Marking {
		EMPTY("empty"),
		FALSE_TOKEN("false"),
		TRUE_TOKEN("true");

		public final String name;

		private Marking(String name) {
			this.name = name;
		}
	}

	public enum SynchronisationType {
		PLAIN("plain"),
		AND("and"),
		OR("or");

		public final String name;

		private SynchronisationType(String name) {
			this.name = name;
		}
	}

	private Marking marking = Marking.EMPTY;
	private SynchronisationType synchronisationType = SynchronisationType.PLAIN;

	public Marking getMarking() {
		return marking;
	}

	public void setMarking(Marking value) {
		this.marking = value;
		sendNotification(new PropertyChangedEvent(this, "marking"));
	}

	public boolean isFalseMarked() {
		return (this.marking == Marking.FALSE_TOKEN);
	}

	public boolean isTrusMarked() {
		return (this.marking == Marking.TRUE_TOKEN);
	}

	public SynchronisationType getSynchronisationType() {
		return synchronisationType;
	}

	public void setSynchronisationType(SynchronisationType value) {
		this.synchronisationType = value;
		sendNotification(new PropertyChangedEvent(this, "synchronisation type"));
	}

}
