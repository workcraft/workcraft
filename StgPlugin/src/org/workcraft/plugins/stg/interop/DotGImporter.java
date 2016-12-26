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
            return new DotGParser(in).parse();
        } catch (FormatException | ParseException e) {
            throw new DeserialisationException(e);
        }
    }

}
