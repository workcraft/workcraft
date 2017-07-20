package org.workcraft.serialisation;

import java.util.UUID;

public class Format {
    public static final UUID workcraftXML = UUID.fromString("6ea20f69-c9c4-4888-9124-252fe4345309");
    public static final UUID defaultVisualXML = UUID.fromString("2fa9669c-a1bf-4be4-8622-007635d672e5");
    public static final UUID STG = UUID.fromString("000199d9-4ac1-4423-b8ea-9017d838e45b");
    public static final UUID SG = UUID.fromString("f309012a-ab89-4036-bb80-8b1a161e8899");
    public static final UUID WTG = UUID.fromString("ff127612-f14d-4afd-90dc-8c74daa4083c");
    public static final UUID SVG = UUID.fromString("99439c3c-753b-46e3-a5d5-6a0993305a2c");
    public static final UUID PS = UUID.fromString("9b5bd9f0-b5cf-11df-8d81-0800200c9a66");
    public static final UUID EPS = UUID.fromString("c6158d84-e242-4f8c-9ec9-3a6cf045b769");
    public static final UUID PDF = UUID.fromString("fa1da69d-3a17-4296-809e-a71f28066fc0");
    public static final UUID PNG = UUID.fromString("c09714a6-cae9-4744-95cb-17ba4d28f5ef");
    public static final UUID DOT = UUID.fromString("f1596b60-e294-11de-8a39-0800200c9a66");
    public static final UUID VERILOG = UUID.fromString("fdd4414e-fd02-4702-b143-09b24430fdd1");
    public static final UUID SDC = UUID.fromString("fd92a9c6-e13a-4785-83ff-1fb6f666b8ed");
    public static final UUID LPN = UUID.fromString("3d3432eb-a993-430f-a47d-a1efb4280cc8");

    public static String getDescription(UUID format) {
        if (format.equals(workcraftXML)) {
            return ".xml (Workcraft math model)";
        } else if (format.equals(defaultVisualXML)) {
            return ".xml (Workcraft visual model)";
        } else if (format.equals(STG)) {
            return ".g (Signal Transition Graph)";
        } else if (format.equals(SG)) {
            return ".sg (State Graph)";
        } else if (format.equals(WTG)) {
            return ".wtg (Waveform Transition Graph)";
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
        } else if (format.equals(DOT)) {
            return ".dot (Graphviz DOT)";
        } else if (format.equals(VERILOG)) {
            return ".v (Verilog netlist)";
        } else if (format.equals(SDC)) {
            return ".sdc (Synopsys Design Constraints)";
        } else if (format.equals(LPN)) {
            return ".lpn (Labeled Petri Net)";
        } else {
            return "Unknown format";
        }
    }

    public static UUID getUUID(String name) {
        if ("workcraftXML".equals(name)) return workcraftXML;
        if ("defaultVisualXML".equals(name)) return defaultVisualXML;
        if ("STG".equals(name)) return STG;
        if ("SG".equals(name)) return SG;
        if ("WTG".equals(name)) return WTG;
        if ("SVG".equals(name)) return SVG;
        if ("PS".equals(name)) return PS;
        if ("EPS".equals(name)) return EPS;
        if ("PDF".equals(name)) return PDF;
        if ("PNG".equals(name)) return PNG;
        if ("DOT".equals(name)) return DOT;
        if ("VERILOG".equals(name)) return VERILOG;
        if ("SDC".equals(name)) return SDC;
        if ("LPN".equals(name)) return LPN;
        return null;
    }

}
