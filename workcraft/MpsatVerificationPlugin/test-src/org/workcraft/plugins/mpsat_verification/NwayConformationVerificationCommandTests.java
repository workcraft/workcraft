package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.NwayConformationVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NwayConformationVerificationCommandTests {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testHoldNwayConformationVerification() throws DeserialisationException {
        testConformationNwayVerificationCommand(true,
                "block1.stg.work",
                "block2.stg.work",
                "block3.stg.work");
    }

    @Test
    public void testViolateNwayConformationVerification() throws DeserialisationException {
        testConformationNwayVerificationCommand(false,
                 "block1-bad.stg.work",
                "block2.stg.work",
                "block3.stg.work");
    }

    @Test
    public void testFailNwayConformationVerification() throws DeserialisationException {
        testConformationNwayVerificationCommand(null,
                "block1.stg.work");
    }

    @Test
    public void testFailIncorrectNwayConformationVerification() throws DeserialisationException {
        testConformationNwayVerificationCommand(null,
                "block.stg.work",
                "block.stg.work");
    }

    private String getPath(String fileName) {
        String resourcePath = PackageUtils.getPackagePath(getClass(), fileName);
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(resourcePath);
        return url == null ? null : url.getFile();
    }

    private List<String> getPaths(String... fileNames) {
        List<String> result = new ArrayList<>();
        for (String fileName : fileNames) {
            String path = getPath(fileName);
            if ((path != null) && !path.isEmpty()) {
                result.add(path);
            }
        }
        return result;
    }

    private void testConformationNwayVerificationCommand(Boolean result, String... workNames) {
        String data = String.join(" ", getPaths(workNames));
        NwayConformationVerificationCommand command = new NwayConformationVerificationCommand();
        Assertions.assertEquals(result, command.execute(null, command.deserialiseData(data)));
    }

}
