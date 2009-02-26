package org.workcraft.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation type used to associate mathematical objects
 * with their visual counterparts. This is required to construct
 * visual model objects from given mathematical model.
 * @author Ivan Poliakov
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DisplayName {
	String value();
}