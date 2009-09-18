package org.workcraft.dom.visual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.workcraft.gui.graph.tools.GraphEditorTool;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CustomToolButtons {
	Class <? extends GraphEditorTool>[] value();
}
