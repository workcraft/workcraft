package org.workcraft.util;

import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.workspace.WorkspaceEntry;

public class WorkspaceUtils {
	public static boolean canHas(WorkspaceEntry entry, Class<?> cls)	{
		return getAs(entry, cls)!=null;
	}
	@SuppressWarnings("unchecked")
	public static <T> T getAs(WorkspaceEntry entry, Class<T> cls) {
		final Object obj = entry.getObject();
		if(cls.isInstance(obj))
			return (T)obj;
		if(obj instanceof VisualModel)
		{
			final Model mathModel = ((VisualModel) obj).getMathModel();
			if(cls.isInstance(mathModel))
				return (T) mathModel;
		}
		return null;
	}
}
