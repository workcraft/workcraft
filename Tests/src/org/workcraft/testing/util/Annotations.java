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

package org.workcraft.testing.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@interface Bojo {
    String value();
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
            System.out.println(aa.getClass().getSimpleName());
            System.out.println(aa.toString());
        }

        for (Annotation aa : b.getClass().getAnnotations()) {
            System.out.println(aa.getClass().getSimpleName());
            System.out.println(aa.toString());
        }

    }
}
