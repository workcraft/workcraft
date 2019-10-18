package org.workcraft.plugins.xbm.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.interop.Format;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.settings.CommonDebugSettings;
import org.workcraft.plugins.xbm.Xbm;
import org.workcraft.plugins.xbm.XbmDescriptor;
import org.workcraft.plugins.xbm.jj.BmParser;
import org.workcraft.plugins.xbm.jj.ParseException;
import org.workcraft.workspace.ModelEntry;

import java.io.InputStream;

public class BmImporter implements Importer {

    @Override
    public Format getFormat() {
        return BmFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new XbmDescriptor(), importXbm(in));
    }

    public Xbm importXbm(InputStream in) throws DeserialisationException {
        try {
            BmParser parser = new BmParser(in);
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
