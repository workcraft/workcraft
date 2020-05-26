package org.workcraft.plugins.builtin.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public final class PdfFormat implements Format {

    private static PdfFormat instance = null;

    private PdfFormat() {
    }

    public static PdfFormat getInstance() {
        if (instance == null) {
            instance = new PdfFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("fa1da69d-3a17-4296-809e-a71f28066fc0");
    }

    @Override
    public String getName() {
        return "PDF";
    }

    @Override
    public String getExtension() {
        return ".pdf";
    }

    @Override
    public String getDescription() {
        return "Portable Document Format";
    }

}
