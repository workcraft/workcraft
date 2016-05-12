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

package org.workcraft.plugins.stg.interop;

import java.io.File;
import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.jj.DotGParser;
import org.workcraft.plugins.stg.jj.ParseException;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;

public class DotGImporter implements Importer {

    private static final String GRAPH_KEYWORD = ".graph";

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".g")
                && FileUtils.fileContainsKeyword(file, GRAPH_KEYWORD);
    }

    @Override
    public String getDescription() {
        return "Signal Transition Graph (.g)";
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new StgDescriptor(), importSTG(in));
    }

    public StgModel importSTG(InputStream in) throws DeserialisationException {
        try {
            StgModel result = new DotGParser(in).parse();
            return result;
        } catch (FormatException e) {
            throw new DeserialisationException(e);
        } catch (ParseException e) {
            throw new DeserialisationException(e);
        }
    }

}
