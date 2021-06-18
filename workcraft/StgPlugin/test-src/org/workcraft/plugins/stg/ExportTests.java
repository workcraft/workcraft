package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.builtin.interop.*;
import org.workcraft.plugins.stg.interop.LpnFormat;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.net.URL;

class ExportTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testVmeCircuitExport() throws DeserialisationException, IOException, SerialisationException {
        Framework framework = Framework.getInstance();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        ModelEntry me = we.getModelEntry();
        File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(workName));

        // .g
        String gHeader = Info.getGeneratedByText("# STG file ", "\n") +
                ".model Untitled\n" +
                ".inputs dsr dsw ldtack\n" +
                ".outputs d dtack lds\n" +
                ".graph\n";

        File gFile = new File(directory, "export.g");
        framework.exportModel(me, gFile, StgFormat.getInstance());
        Assertions.assertEquals(gHeader, FileUtils.readHeaderUtf8(gFile, gHeader.length()));

        // .lpn
        String lpnHeader = Info.getGeneratedByText("# LPN file ", "\n") +
                ".name Untitled\n" +
                ".inputs dsr dsw ldtack\n" +
                ".outputs d dtack lds\n" +
                "#@.init_state [000000]\n" +
                ".graph\n";

        File lpnFile = new File(directory, "export.lpn");
        framework.exportModel(me, lpnFile, LpnFormat.getInstance());
        Assertions.assertEquals(lpnHeader, FileUtils.readHeaderUtf8(lpnFile, lpnHeader.length()));

        // .svg
        String svgHeader = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n" +
                "<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.0//EN'%n" +
                "          'http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd'>");

        File svgFile = new File(directory, "export.svg");
        framework.exportModel(me, svgFile, SvgFormat.getInstance());
        Assertions.assertEquals(svgHeader, FileUtils.readHeaderUtf8(svgFile, svgHeader.length()));

        // .png
        String pngHeader =  (char) 0xFFFD + "PNG";
        File pngFile = new File(directory, "export.png");
        framework.exportModel(me, pngFile, PngFormat.getInstance());
        Assertions.assertEquals(pngHeader, FileUtils.readHeaderUtf8(pngFile, pngHeader.length()));

        // .pdf
        String pdfHeader = "%PDF-1.4";
        File pdfFile = new File(directory, "export.pdf");
        framework.exportModel(me, pdfFile, PdfFormat.getInstance());
        Assertions.assertEquals(pdfHeader, FileUtils.readHeaderUtf8(pdfFile, pdfHeader.length()));

        // .eps
        String epsHeader = "%!PS-Adobe-3.0 EPSF-3.0";
        File epsFile = new File(directory, "export.eps");
        framework.exportModel(me, epsFile, EpsFormat.getInstance());
        Assertions.assertEquals(epsHeader, FileUtils.readHeaderUtf8(epsFile, epsHeader.length()));

        // .ps
        String psHeader = "%!PS-Adobe-3.0";
        File psFile = new File(directory, "export.ps");
        framework.exportModel(me, psFile, PsFormat.getInstance());
        Assertions.assertEquals(psHeader, FileUtils.readHeaderUtf8(psFile, psHeader.length()));

        // .dot
        String dotHeader = String.format(
                "digraph work {%n" +
                "  graph [overlap=false, splines=true, nodesep=1.0, ranksep=1.0, rankdir=TB];%n" +
                "  node [shape=box, fixedsize=true];%n");

        File dotFile = new File(directory, "export.dot");
        framework.exportModel(me, dotFile, DotFormat.getInstance());
        Assertions.assertEquals(dotHeader, FileUtils.readHeaderUtf8(dotFile, dotHeader.length()));

        framework.closeWork(we);
        FileUtils.deleteOnExitRecursively(directory);
    }

}
