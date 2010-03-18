package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.petri.Place;

@VisualClass("org.workcraft.plugins.petri.VisualPlace")
@DisplayName("Place")
public class STGPlace extends Place {
	private boolean implicit = false;
	private int capacity = 1;

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int c) {
		this.capacity = c;

		sendNotification ( new PropertyChangedEvent (this, "capacity"));
	}

	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
	}

	public boolean isImplicit() {
		return implicit;
	}
}