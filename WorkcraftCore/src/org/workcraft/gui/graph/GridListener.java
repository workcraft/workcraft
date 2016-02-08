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

package org.workcraft.gui.graph;

/**
 * The <code>GridListener</code> interface defines methods for classes that wish
 * to be notified of the changes in grid parameters.
 * @author Ivan Poliakov
 *
 */
public interface GridListener {
    /**
     * The grid parameters (such as number of visible lines, major and minor line intervals, etc.) have changed.
     * @param sender
     * The grid that sent the event.
     */
    public void gridChanged(Grid sender);
}
