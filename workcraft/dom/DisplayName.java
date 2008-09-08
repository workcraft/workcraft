package org.workcraft.dom;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DisplayName {
	String value();
}