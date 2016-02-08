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
 * Utility class that represents a horizontal or vertical guideline that facilitates editing by allowing
 * objects to be "snapped" to it.
 *  *
 * @author Ivan Poliakov
 *
 */
public class Guideline {
    enum GuidelineType {
        HORIZONTAL_GUIDE,
        VERTICAL_GUIDE
    }

    protected GuidelineType type;
    protected double position;

    public Guideline(GuidelineType type, double position) {
        this.type = type;
        this.position = position;
    }

    public double getPosition() {
        return position;
    }

    public GuidelineType getType() {
        return type;
    }
}