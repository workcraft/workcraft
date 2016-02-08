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

package org.workcraft.plugins.petri;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.petri.VisualPlace.class)
public class Place extends MathNode {
    public static final String PROPERTY_CAPACITY = "Capacity";
    public static final String PROPERTY_TOKENS = "Tokens";

    protected int tokens = 0;
    protected int capacity = 1;

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int value) {
        if (value != tokens) {
            if (value < 0) {
                throw new ArgumentException("The number of tokens cannot be negative.");
            }
            if (value > capacity) {
                setCapacity(value);
            }
            this.tokens = value;
            sendNotification( new PropertyChangedEvent(this, PROPERTY_TOKENS) );
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int value) {
        if (value != capacity) {
            if (value < 1) {
                throw new ArgumentException("Negative or zero capacity is not allowed.");
            }
            if (tokens > value) {
                throw new ArgumentException("The place capacity "+ value + " is too small for the current number of tokens " + tokens + " .");
            }
            this.capacity = value;
            sendNotification ( new PropertyChangedEvent (this, PROPERTY_CAPACITY));
        }
    }

}
