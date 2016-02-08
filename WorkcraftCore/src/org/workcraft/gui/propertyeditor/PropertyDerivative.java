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
import java.util.Map;

public class PropertyDerivative implements PropertyDescriptor {
    final PropertyDescriptor descriptor;

    public PropertyDerivative(PropertyDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public Class<?> getType() {
        return descriptor.getType();
    }

    @Override
    public boolean isWritable() {
        return descriptor.isWritable();
    }

    @Override
    public boolean isCombinable() {
        return descriptor.isCombinable();
    }

    @Override
    public boolean isTemplatable() {
        return descriptor.isTemplatable();
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return descriptor.getValue();
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        descriptor.setValue(value);
    }

    @Override
    public Map<? extends Object, String> getChoice() {
        return descriptor.getChoice();
    }

}
