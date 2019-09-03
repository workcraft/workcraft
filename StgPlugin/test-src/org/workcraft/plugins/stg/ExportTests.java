package org.workcraft.plugins.stg;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.builtin.interop.DotFormat;
import org.workcraft.plugins.builtin.interop.EpsFormat;
import org.workcraft.plugins.builtin.interop.PdfFormat;
import org.workcraft.plugins.builtin.interop.PngFormat;
import org.workcraft.plugins.builtin.interop.PsFormat;
import org.workcraft.plugins.builtin.interop.SvgFormat;
import org.workcraft.plugins.stg.interop.LpnFormat;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ExportTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testVmeCircuitExport() throws DeserialisationException, IOException, SerialisationException {
        String gHeader = Info.getGeneratedByText("# STG file ", "\n") +
                ".model Untitled\n" +
                ".inputs dsr dsw ldtack\n" +
                ".outputs d dtack lds\n" +
                ".graph\n";

        String lpnHeader = Info.getGeneratedByText("# LPN file ", "\n") +
                ".name Untitled\n" +
                "#@.init_state [000000]\n" +
                ".inputs dsr dsw ldtack\n" +
                ".outputs d dtack lds\n" +
                ".graph\n";

        String svgHeader = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n" +
                "<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.0//EN'%n" +
                "          'http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd'>");

        String pngHeader =  (char) 0xFFFD + "PNG";

        String pdfHeader = "%PDF-1.4";

        String epsHeader = "%!PS-Adobe-3.0 EPSF-3.0";

        String psHeader = "%!PS-Adobe-3.0";

        String dotHeader = String.format(
                "digraph work {%n" +
                "  graph [overlap=false, splines=true, nodesep=1.0, ranksep=1.0, rankdir=LR];%n" +
                "  node [shape=box, fixedsize=true];%n");

        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testCircuitExport(workName, gHeader, lpnHeader, svgHeader, pngHeader, pdfHeader, epsHeader, psHeader, dotHeader);
    }

    public void testCircuitExport(String workName, String gHeader, String lpnHeader,
            String svgHeader, String pngHeader, String pdfHeader, String epsHeader, String psHeader, String dotHeader)
                    throws DeserialisationException, IOException, SerialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        ModelEntry me = we.getModelEntry();
        File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(workName));

        File gFile = new File(directory, "export.g");
        framework.exportModel(me, gFile, StgFormat.getInstance());
        Assert.assertEquals(gHeader, FileUtils.readHeaderUtf8(gFile, gHeader.length()));

        File lpnFile = new File(directory, "export.lpn");
        framework.exportModel(me, lpnFile, LpnFormat.getInstance());
        Assert.assertEquals(lpnHeader, FileUtils.readHeaderUtf8(lpnFile, lpnHeader.length()));

        File svgFile = new File(directory, "export.svg");
        framework.exportModel(me, svgFile, SvgFormat.getInstance());
        Assert.assertEquals(svgHeader, FileUtils.readHeaderUtf8(svgFile, svgHeader.length()));

        File pngFile = new File(directory, "export.png");
        framework.exportModel(me, pngFile, PngFormat.getInstance());
        Assert.assertEquals(pngHeader, FileUtils.readHeaderUtf8(pngFile, pngHeader.length()));

        File pdfFile = new File(directory, "export.pdf");
        framework.exportModel(me, pdfFile, PdfFormat.getInstance());
        Assert.assertEquals(pdfHeader, FileUtils.readHeaderUtf8(pdfFile, pdfHeader.length()));

        File epsFile = new File(directory, "export.eps");
        framework.exportModel(me, epsFile, EpsFormat.getInstance());
        Assert.assertEquals(epsHeader, FileUtils.readHeaderUtf8(epsFile, epsHeader.length()));

        File psFile = new File(directory, "export.ps");
        framework.exportModel(me, psFile, PsFormat.getInstance());
        Assert.assertEquals(psHeader, FileUtils.readHeaderUtf8(psFile, psHeader.length()));

        File dotFile = new File(directory, "export.dot");
        framework.exportModel(me, dotFile, DotFormat.getInstance());
        Assert.assertEquals(dotHeader, FileUtils.readHeaderUtf8(dotFile, dotHeader.length()));

        framework.closeWork(we);
        FileUtils.deleteOnExitRecursively(directory);
    }

}
