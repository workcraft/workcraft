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

package org.workcraft.history;

public class HistoryEvent {
    private String undoScript;
    private String redoScript;
    private String eventDescription;

    public HistoryEvent (String undoScript, String redoScript, String eventDescription, Object sender) {
        //super(sender);
        this.undoScript = undoScript;
        this.redoScript = redoScript;
        this.eventDescription = eventDescription;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getRedoScript() {
        return redoScript;
    }

    public String getUndoScript() {
        return undoScript;
    }

}
