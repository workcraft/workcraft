package org.workcraft.plugins.builtin.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class SvgExportUtils {

    private static final int BUF_SIZE = 1024;

    /*
     * FIXME: Temporary use triple modular redundancy to prevent corrupted Batik stream.
     * Sometimes the result of Batik export gets corrupted. In order to prevent this from
     * propagating to the output file a majority voting is (temporary) used.
     */
    public static InputStream streamTripleVote(Model model) throws IOException, SerialisationException {
        InputStream result = stream(model);
        if (!isSame(result, stream(model))) {
            result = stream(model);
        }
        return result;
    }

    public static InputStream stream(Model model) throws IOException, SerialisationException {
        SvgExporter svgExporter = new SvgExporter();
        ByteArrayOutputStream svgOut = new ByteArrayOutputStream();
        svgExporter.export(model, svgOut);
        return new ByteArrayInputStream(svgOut.toByteArray());
    }

    private static boolean isSame(InputStream is1, InputStream is2) throws IOException {
        byte[] buf1 = new byte[BUF_SIZE];
        byte[] buf2 = new byte[BUF_SIZE];
        try {
            while (true) {
                int cnt1 = is1.read(buf1);
                int cnt2 = is2.read(buf2);
                if (cnt1 < 0) {
                    return cnt2 < 0;
                }
                if ((cnt2 != cnt1) || !Arrays.equals(buf1, buf2)) {
                    return false;
                }
            }
        } finally {
            is1.reset();
            is2.reset();
        }
    }

}
