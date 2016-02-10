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

package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;

public final class MethodParametersMatcher<T extends MethodParametersMatcher.MethodInfo> {
    private MethodParametersMatcher() {
    }

    public interface MethodInfo {
        Class<?>[] getParameterTypes();
    }

    private ArrayList<T> methods;

    public static <T extends MethodParametersMatcher.MethodInfo> T match(Collection<T> methods, Class<?>... parameters) throws NoSuchMethodException {
        MethodParametersMatcher<T> matcher = new MethodParametersMatcher<T>();
        return matcher.instanceMatch(methods, parameters);
    }

    private T instanceMatch(Collection<T> methods, Class<?>... parameters) throws NoSuchMethodException {
        this.methods = new ArrayList<T>(methods);

        matchByParameters(parameters);

        filtered = new boolean[this.methods.size()];
        for(int i=0;i<parameters.length;i++)
            filterByParameter(i);

        for(int i=this.methods.size();--i>=0;)
            if(filtered[i])
                remove(i);

        if(this.methods.size() > 1)
            throw new RuntimeException("We have a bug o_O");

        if(this.methods.size() < 1) {
            if(filtered.length > 1)
                throw new AmbiguousMethodException();
            else
                if(filtered.length == 0)
                    throw new NoSuchMethodException("Constructor not found");
                else
                    throw new RuntimeException("We have a bug o_O");
        }

        return this.methods.get(0);
    }

    private void matchByParameters(Class<?>[] parameters) {
        for(int i=methods.size(); --i>=0;)
            if(!matches(methods.get(i).getParameterTypes(), parameters))
                remove(i);
    }

    private boolean matches(Class<?>[] actual, Class<?>[] expected) {
        if(expected.length != actual.length)
            return false;
        for(int i=0;i<expected.length;i++)
            if(!actual[i].isAssignableFrom(expected[i]))
                return false;
        return true;
    }

    boolean[] filtered;

    private void filterByParameter(int parameterNumber) {
        Class<?> best = null;

        for(int i=methods.size(); --i>=0;) {
            Class<?> current = methods.get(i).getParameterTypes()[parameterNumber];
            if(best == null || best.isAssignableFrom(current))
                best = current;
        }
        for(int i=methods.size(); --i>=0;)
            if(methods.get(i).getParameterTypes()[parameterNumber] != best)
                filtered[i] = true;
    }

    void remove(int i) {
        int last = methods.size()-1;
        methods.set(i, methods.get(last));
        methods.remove(last);
    }
}
