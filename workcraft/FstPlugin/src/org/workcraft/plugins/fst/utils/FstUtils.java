package org.workcraft.plugins.fst.utils;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.Signal;
import org.workcraft.plugins.fst.interop.SgImporter;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FstUtils {

    public static Color getTypeColor(Signal.Type type) {
        if (type != null) {
            switch (type) {
            case INPUT:    return SignalCommonSettings.getInputColor();
            case OUTPUT:   return SignalCommonSettings.getOutputColor();
            case INTERNAL: return SignalCommonSettings.getInternalColor();
            default:       return SignalCommonSettings.getDummyColor();
            }
        }
        return Color.BLACK;
    }

    public static Fst importFst(byte[] bytes) {
        return importFst(new ByteArrayInputStream(bytes));
    }

    public static Fst importFst(InputStream is) {
        SgImporter importer = new SgImporter();
        try {
            return importer.importSG(is);
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }

    }

}
