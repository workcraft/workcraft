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

package org.workcraft.gui.graph.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.workcraft.annotations.Annotations;

public class ListenerResolver {
	private HashMap<Class<?>, Collection<GraphEditorMouseListener>> listeners = new HashMap<Class<?>, Collection<GraphEditorMouseListener>>();

	private void processMouseListeners (Class<?> currentLevel, Collection<GraphEditorMouseListener> list) {
		Class<? extends GraphEditorMouseListener> mouseListeners[] = Annotations.getMouseListeners(currentLevel);
		if (mouseListeners != null) {
			try {
				for (Class<? extends GraphEditorMouseListener> cls : mouseListeners)
					list.add(cls.newInstance());
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		if (currentLevel.getSuperclass() != Object.class)
			processMouseListeners (currentLevel.getSuperclass(), list);
	}

	public Collection<GraphEditorMouseListener> getMouseListenersFor(Class<?> cls) {
		Collection<GraphEditorMouseListener> list = listeners.get(cls);

		if (list==null)
		{
			list = new LinkedList<GraphEditorMouseListener>();
			processMouseListeners(cls, list);
			listeners.put(cls, list);
		}

		return list;
	}
}