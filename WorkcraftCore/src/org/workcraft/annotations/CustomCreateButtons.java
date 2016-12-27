package org.workcraft.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.workcraft.gui.graph.generators.NodeGenerator;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CustomCreateButtons {
    Class<? extends NodeGenerator>[] value();
}
