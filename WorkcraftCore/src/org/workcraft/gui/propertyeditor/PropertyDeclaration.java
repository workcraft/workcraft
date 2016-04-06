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

package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

abstract public class PropertyDeclaration<O, V> implements PropertyDescriptor, Disableable {
    private final O object;
    private String name;
    private Class<V> cls;
    private boolean writable;
    private boolean combinable;
    private boolean templatable;

    public PropertyDeclaration(O object, String name, Class<V> cls, boolean writable, boolean combinable, boolean templatable) {
        this.object = object;
        this.name = name;
        this.cls = cls;
        this.writable = writable;
        this.combinable = combinable;
        this.templatable = templatable;
    }

    abstract protected void setter(O object, V value);

    abstract protected V getter(O object);

    @Override
    public Map<V, String> getChoice() {
        LinkedHashMap<V, String> result = null;
        if (cls.isEnum()) {
            result = new LinkedHashMap<V, String>();
            for (V item : cls.getEnumConstants()) {
                result.put(item, item.toString());
            }
        }
        return result;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return getter(object);
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        try {
            setter(object, cls.cast(value));
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<V> getType() {
        return cls;
    }

    @Override
    public boolean isWritable() {
        return writable;
    }

    @Override
    public boolean isCombinable() {
        return combinable;
    }

    @Override
    public boolean isTemplatable() {
        return templatable;
    }

    public O getObject() {
        return object;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}