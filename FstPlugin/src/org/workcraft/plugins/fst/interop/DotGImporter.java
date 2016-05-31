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

package org.workcraft.plugins.fst.interop;

import java.io.File;
import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.fst.FstDescriptor;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.jj.DotGParser;
import org.workcraft.plugins.fst.jj.ParseException;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;

public class DotGImporter implements Importer {

    private static final String STATEGRAPH_KEYWORD = ".state graph";

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".sg")
                && FileUtils.fileContainsKeyword(file, STATEGRAPH_KEYWORD);
    }

    @Override
    public String getDescription() {
        return "State Graph (.sg)";
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new FstDescriptor(), importSG(in));
    }

    public Fst importSG(InputStream in) throws DeserialisationException {
        try {
            return new DotGParser(in).parse();
        } catch (FormatException e) {
            throw new DeserialisationException(e);
        } catch (ParseException e) {
            throw new DeserialisationException(e);
        }
    }
}
