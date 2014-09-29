package org.workcraft.plugins.son.connections;

import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

public class SONConnection extends MathConnection{

	public enum Semantics {
		PNLINE("Petri net connection"),
		SYNCLINE("Synchronous communication"),
		ASYNLINE("Asynchronous communication"),
		BHVLINE("Behavioural abstraction");

		private final String name;

		private Semantics(String name) {
			this.name = name;
		}

		static public Map<String, Semantics> getChoice() {
			LinkedHashMap<String, Semantics> choice = new LinkedHashMap<String, Semantics>();
			for (Semantics item : Semantics.values()) {
				choice.put(item.name, item);
			}
			return choice;
		}

		public String toString() {
			switch (this) {
			case PNLINE: return "POLYLINE";
			case SYNCLINE: return "SYNCLINE";
			case ASYNLINE: return "ASYNLINE";
			case BHVLINE: return "BHVLINE";
			default: return "";
			}
		}
	};

	private Semantics semantics;

	public SONConnection(){
	}

	public SONConnection(MathNode first, MathNode second, Semantics semantics) {
		super();
		this.setSemantics(semantics);
	}

	public Semantics getSemantics() {
		return semantics;
	}

	public void setSemantics(Semantics semantics) {
		this.semantics = semantics;
		sendNotification(new PropertyChangedEvent(this, "semantics"));
	}

}
