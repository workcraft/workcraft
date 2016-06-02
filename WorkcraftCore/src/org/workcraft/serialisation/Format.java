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

package org.workcraft.serialisation;

import java.util.UUID;

public class Format {
    public static final UUID workcraftXML = UUID.fromString("6ea20f69-c9c4-4888-9124-252fe4345309");
    public static final UUID defaultVisualXML = UUID.fromString("2fa9669c-a1bf-4be4-8622-007635d672e5");
    public static final UUID STG = UUID.fromString("000199d9-4ac1-4423-b8ea-9017d838e45b");
    public static final UUID SG = UUID.fromString("f309012a-ab89-4036-bb80-8b1a161e8899");
    public static final UUID SVG = UUID.fromString("99439c3c-753b-46e3-a5d5-6a0993305a2c");
    public static final UUID PS = UUID.fromString("9b5bd9f0-b5cf-11df-8d81-0800200c9a66");
    public static final UUID EPS = UUID.fromString("c6158d84-e242-4f8c-9ec9-3a6cf045b769");
    public static final UUID PDF = UUID.fromString("fa1da69d-3a17-4296-809e-a71f28066fc0");
    public static final UUID PNG = UUID.fromString("c09714a6-cae9-4744-95cb-17ba4d28f5ef");
    public static final UUID EMF = UUID.fromString("c8f9fe37-87d2-42bd-8a25-9e1d38813549");
    public static final UUID DOT = UUID.fromString("f1596b60-e294-11de-8a39-0800200c9a66");
    public static final UUID EQN = UUID.fromString("58b3c8d0-e297-11de-8a39-0800200c9a66");
    public static final UUID VERILOG = UUID.fromString("fdd4414e-fd02-4702-b143-09b24430fdd1");

    public static String getDescription(UUID format) {
        if (format.equals(workcraftXML)) {
            return ".xml (Workcraft math model)";
        } else if (format.equals(defaultVisualXML)) {
            return ".xml (Workcraft visual model)";
        } else if (format.equals(STG)) {
            return ".g (Signal Transition Graph)";
        } else if (format.equals(SG)) {
            return ".sg (State Graph)";
        } else if (format.equals(SVG)) {
            return ".svg (Scalable Vector Graphics)";
        } else if (format.equals(PS)) {
            return ".ps (PostScript)";
        } else if (format.equals(EPS)) {
            return ".eps (Encapsulated PostScript)";
        } else if (format.equals(PDF)) {
            return ".pdf (Portable Document Format)";
        } else if (format.equals(PNG)) {
            return ".png (Portable Network Graphics)";
        } else if (format.equals(EMF)) {
            return ".emf (Enhanced Mediafile)";
        } else if (format.equals(DOT)) {
            return ".dot (GraphViz dot)";
        } else if (format.equals(EQN)) {
            return ".eqn (Signal equations)";
        } else if (format.equals(VERILOG)) {
            return ".v (Verilog netlist)";
        } else {
            return "Unknown format";
        }
    }

    public static UUID getUUID(String name) {
        if ("workcraftXML".equals(name)) return workcraftXML;
        if ("defaultVisualXML".equals(name)) return defaultVisualXML;
        if ("STG".equals(name)) return STG;
        if ("SG".equals(name)) return SG;
        if ("SVG".equals(name)) return SVG;
        if ("PS".equals(name)) return PS;
        if ("EPS".equals(name)) return EPS;
        if ("PDF".equals(name)) return PDF;
        if ("PNG".equals(name)) return PNG;
        if ("EMF".equals(name)) return EMF;
        if ("DOT".equals(name)) return DOT;
        if ("EQN".equals(name)) return EQN;
        if ("VERILOG".equals(name)) return VERILOG;
        return null;
    }

}
