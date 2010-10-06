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

package org.workcraft.interop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.workspace.ModelEntry;

public interface Importer {
	public boolean accept (File file);
	public String getDescription();
	public ModelEntry importFrom (InputStream in) throws DeserialisationException, IOException;
}