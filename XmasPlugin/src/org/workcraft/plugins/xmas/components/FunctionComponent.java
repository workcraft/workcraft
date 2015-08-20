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

package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.xmas.components.VisualFunctionComponent.class)
public class FunctionComponent extends XmasComponent {
    public static final String PROPERTY_TYPE = "Type";

	public enum Type {
        TYPE_T("t"),
        TYPE_NOT("!");

        private final String name;

        private Type(String name) {
            this.name = name;
        }

		@Override
		public String toString() {
			return name;
		}
    };

    public Type type = Type.TYPE_T;

	public void setType(Type value) {
		if (type != value) {
			type = value;
			sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));
		}
	}

	public Type getType() {
		return type;
	}

}
