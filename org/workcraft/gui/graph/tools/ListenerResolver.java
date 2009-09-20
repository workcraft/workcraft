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