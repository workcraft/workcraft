/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.xmas.components.VisualQueueComponent.class)
public class QueueComponent extends XmasComponent {
	public static final String PROPERTY_CAPACITY = "Capacity";
	public static final String PROPERTY_INIT = "Init";

	public int capacity = 2;
	public int init = 0;

	public void setCapacity(int capacity) {
		this.capacity = capacity;
		sendNotification(new PropertyChangedEvent(this, PROPERTY_CAPACITY));
	}

	public int getCapacity() {
		return capacity;
	}

	public void setInit(int init) {
		this.init = init;
		sendNotification(new PropertyChangedEvent(this, PROPERTY_INIT));
	}

	public int getInit() {
		return init;
	}

}
