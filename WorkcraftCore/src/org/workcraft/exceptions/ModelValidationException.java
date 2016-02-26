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

import java.util.LinkedList;

public class ModelValidationException extends Exception {

    private static final long serialVersionUID = 1L;
    private LinkedList<String> errors = new LinkedList<>();
    public ModelValidationException() {
        super();
    }

    public void addError(String message) {
        errors.add(message);
    }

    public LinkedList<String> getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        String r = "Model contains following errors:\n";
        for (String e: errors) {
            r += e + "\n";
        }
        return r;
    }
}
