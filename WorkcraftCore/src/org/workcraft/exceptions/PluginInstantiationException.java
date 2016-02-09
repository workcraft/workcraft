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

package org.workcraft.exceptions;

public class PluginInstantiationException extends Exception {
    public PluginInstantiationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public PluginInstantiationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public PluginInstantiationException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    private static final long serialVersionUID = 1L;

    public PluginInstantiationException(String reason) {
        super(reason);
    }
}
