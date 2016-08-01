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

package org.workcraft.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.workcraft.PluginProvider;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.workspace.ModelEntry;

public class Import {

    public static ModelEntry importFromFile(Importer importer, File file) throws IOException, DeserialisationException {
        FileInputStream fileInputStream = new FileInputStream(file);
        ModelEntry model = importer.importFrom(fileInputStream);
        fileInputStream.close();
        return model;
    }

    public static Importer chooseBestImporter(PluginProvider provider, File file) {
        for (PluginInfo<? extends Importer> info : provider.getPlugins(Importer.class)) {
            Importer importer = info.getSingleton();

            if (importer.accept(file)) {
                return importer;
            }
        }
        return null;
    }

    public static ModelEntry importFromByteArray(Importer importer, byte[] array) throws IOException, DeserialisationException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(array);
        ModelEntry model = importer.importFrom(inputStream);
        inputStream.close();
        return model;
    }

}
