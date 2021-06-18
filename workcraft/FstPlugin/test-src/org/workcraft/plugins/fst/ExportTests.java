package org.workcraft.plugins.fst;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.fst.interop.SgFormat;
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
        String workName = PackageUtils.getPackagePath(getClass(), "vme.fst.work");
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        ModelEntry me = we.getModelEntry();
        File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(workName));

        // .sg
        String sgHeader = Info.getGeneratedByText("# SG file ", "\n") +
                ".model VME\n" +
                ".inputs dsr dsw ldtack\n" +
                ".outputs d lds dtack\n" +
                ".state graph\n";

        File sgFile = new File(directory, "export.sg");
        framework.exportModel(me, sgFile, SgFormat.getInstance());
        Assertions.assertEquals(sgHeader, FileUtils.readHeaderUtf8(sgFile, sgHeader.length()));

        framework.closeWork(we);
        FileUtils.deleteOnExitRecursively(directory);
    }

}
