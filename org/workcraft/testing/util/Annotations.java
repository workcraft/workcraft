package org.workcraft.testing.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@interface Bojo {
	public String value();
}

@Bojo("qwe")
class A {

}

//@Bojo("xru")
class B extends A {

}


public class Annotations {
	@Test
	public void test() {
		A a = new A();
		B b = new B();

		for (Annotation aa : a.getClass().getAnnotations()) {
			System.out.println (aa.getClass().getSimpleName());
			System.out.println (aa.toString());
		}

		for (Annotation aa : b.getClass().getAnnotations()) {
			System.out.println (aa.getClass().getSimpleName());
			System.out.println (aa.toString());
		}

	}
}
