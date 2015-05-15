package org.workcraft.plugins.son.connections;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

public class SONConnection extends MathConnection{

	private String time = "0000-9999";

	public enum Semantics {
		PNLINE("Petri net connection"),
		SYNCLINE("Synchronous communication"),
		ASYNLINE("Asynchronous communication"),
		BHVLINE("Behavioural abstraction");

		private final String name;

		private Semantics(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Semantics semantics = Semantics.PNLINE;

	public SONConnection(){
	}

	public SONConnection(MathNode first, MathNode second, Semantics semantics) {
		super(first, second);
		this.setSemantics(semantics);
	}

	public Semantics getSemantics() {
		return semantics;
	}

	public void setSemantics(Semantics semantics) {
		this.semantics = semantics;
		sendNotification(new PropertyChangedEvent(this, "semantics"));
	}

	public String getTime(){
		return time;
	}

	public void setTime(String time){
		this.time = time;
		sendNotification(new PropertyChangedEvent(this, "time interval"));
	}
}
