/*
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All rights reserved.
 */

package org.workcraft.plugins.circuit.serialisation;

import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.interop.SdcFormat;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PathbreakConstraintExporter implements PathbreakExporter {

    @Override
    public SdcFormat getFormat() {
        return SdcFormat.getInstance();
    }

    @Override
    public void export(Circuit circuit, File file) {
        LogUtils.logInfo(ExportUtils.getExportMessage(circuit, file, "Writing path breaker constraints for the circuit "));
        PathbreakConstraintSerialiser serialiser = new PathbreakConstraintSerialiser();
        try {
            FileOutputStream out = new FileOutputStream(file);
            serialiser.serialise(circuit, out);
            out.close();
        } catch (IOException e) {
            LogUtils.logError("Could not write into file '" + file.getAbsolutePath() + "'");
        }
    }

}
