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
