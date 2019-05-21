/*
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All rights reserved.
 */

package org.workcraft.plugins.circuit.serialisation;

import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.serialisation.SerialFormat;

import java.io.OutputStream;

public interface PathbreakSerialiser extends SerialFormat {
    String getName();
    void serialise(Circuit circuit, OutputStream out);
}
