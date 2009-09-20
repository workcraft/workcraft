package org.workcraft.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.workcraft.gui.graph.tools.GraphEditorMouseListener;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MouseListeners {
	public Class<? extends GraphEditorMouseListener>[] value();
}
