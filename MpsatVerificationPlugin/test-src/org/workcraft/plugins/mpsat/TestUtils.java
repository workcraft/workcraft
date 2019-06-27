package org.workcraft.plugins.mpsat;

import org.workcraft.utils.DesktopApi;

public class TestUtils {

    public static String getUnfoldingToolsPath(String fileName) {
        switch (DesktopApi.getOs()) {
        case LINUX:
            return "dist-template/linux/tools/UnfoldingTools/" + fileName;
        case MACOS:
            return "dist-template/osx/Contents/Resources/tools/UnfoldingTools/" + fileName;
        case WINDOWS:
            return "dist-template\\windows\\tools\\UnfoldingTools\\" + fileName;
        }
        return fileName;
    }

}
