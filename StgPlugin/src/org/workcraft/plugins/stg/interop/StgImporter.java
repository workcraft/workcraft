package org.workcraft.plugins.stg.interop;

import java.io.File;
import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.jj.StgParser;
import org.workcraft.plugins.stg.jj.ParseException;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;

public class StgImporter implements Importer {

    private static final String GRAPH_KEYWORD = ".graph";

    @Override
    public StgFormat getFormat() {
        return StgFormat.getInstance();
    }

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".g")
                && FileUtils.fileContainsKeyword(file, GRAPH_KEYWORD);
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new StgDescriptor(), importStg(in));
    }

    public StgModel importStg(InputStream in) throws DeserialisationException {
        try {
            StgParser parser = new StgParser(in);
            if (CommonDebugSettings.getParserTracing()) {
                parser.enable_tracing();
            } else {
                parser.disable_tracing();
            }
            return parser.parse();
        } catch (FormatException | ParseException e) {
            throw new DeserialisationException(e);
        }
    }

}
