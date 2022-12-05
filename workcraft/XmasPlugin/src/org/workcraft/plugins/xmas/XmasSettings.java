package org.workcraft.plugins.xmas;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractModelSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class XmasSettings extends AbstractModelSettings {

    private static final String VSETTINGS_FILE_NAME = "vsettings";
    private static final String VSETTINGS_DEFAULT_CONTENT = "trace q1\nlevel normal\nhighlight none\ndisplay popup\nsoln 2\n";
    private static final String CPN_FILE_NAME = "CPNFile";
    private static final String IN_FILE_NAME = "in";
    private static final String PNC_FILE_NAME = "PNCFile";
    private static final String JSON_FILE_NAME = "JsonFile";
    private static final String SOLN_FILE_NAME = "soln";
    private static final String QSL_FILE_NAME = "qsl";
    private static final String EQU_FILE_NAME = "equ";
    private static final String QUE_FILE_NAME = "que";
    private static final String LOC_FILE_NAME = "loc";
    private static final String SYNC_FILE_NAME = "sync";
    private static final String QLIST_FILE_NAME = "qlist";

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "XmasSettings";

    private static final String keyShowContacts = prefix + ".showContacts";
    private static final String keyBorderWidth = prefix + ".borderWidth";
    private static final String keyWireWidth = prefix + ".wireWidth";
    private static final String keyVxmCommand = prefix + ".vxmCommand";

    private static final boolean defaultShowContacts = false;
    private static final double defaultBorderWidth = 0.06;
    private static final double defaultWireWidth = 0.06;
    private static final String defaultVxmCommand = BackendUtils.getToolPath("vxm", "vxm");

    private static boolean showContacts = defaultShowContacts;
    private static double borderWidth = defaultBorderWidth;
    private static double wireWidth = defaultWireWidth;
    private static String vxmCommand = defaultVxmCommand;

    private static File vxmTempDirectory = null;

    static {
        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show contacts",
                XmasSettings::setShowContacts,
                XmasSettings::getShowContacts));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Border width",
                XmasSettings::setBorderWidth,
                XmasSettings::getBorderWidth));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Wire width",
                XmasSettings::setWireWidth,
                XmasSettings::getWireWidth));

        properties.add(new PropertyDeclaration<>(String.class,
                "VXM cmmand",
                XmasSettings::setVxmCommand,
                XmasSettings::getVxmCommand));
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setShowContacts(config.getBoolean(keyShowContacts, defaultShowContacts));
        setBorderWidth(config.getDouble(keyBorderWidth, defaultBorderWidth));
        setWireWidth(config.getDouble(keyWireWidth, defaultWireWidth));
        setVxmCommand(config.getString(keyVxmCommand, defaultVxmCommand));
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyShowContacts, getShowContacts());
        config.setDouble(keyBorderWidth, getBorderWidth());
        config.setDouble(keyWireWidth, getWireWidth());
        config.set(keyVxmCommand, getVxmCommand());
    }

    @Override
    public String getName() {
        return "xMAS Circuit";
    }

    public static boolean getShowContacts() {
        return showContacts;
    }

    public static void setShowContacts(boolean value) {
        showContacts = value;
    }

    public static double getBorderWidth() {
        return borderWidth;
    }

    public static void setBorderWidth(double value) {
        borderWidth = value;
    }

    public static double getWireWidth() {
        return wireWidth;
    }

    public static void setWireWidth(double value) {
        wireWidth = value;
    }

    public static String getVxmCommand() {
        return vxmCommand;
    }

    public static void setVxmCommand(String value) {
        vxmCommand = value;
    }

    public static File getTempVxmDirectory() {
        if (vxmTempDirectory == null) {
            vxmTempDirectory = FileUtils.createTempDirectory("vxm-");
            File vxmFile = new File(getVxmCommand());
            if (FileUtils.checkFileReadability(vxmFile, false, "VXM access error")) {
                File vxmDirectory = vxmFile.getParentFile();
                File vsettingsFile = new File(vxmDirectory, VSETTINGS_FILE_NAME);
                try {
                    if (vsettingsFile.exists() && vsettingsFile.isFile() && vsettingsFile.canRead()) {
                        FileUtils.copyAll(vsettingsFile, vxmTempDirectory);
                    } else {
                        File vsettingsTempFile = new File(vxmTempDirectory, VSETTINGS_FILE_NAME);
                        FileUtils.writeAllText(vsettingsTempFile, VSETTINGS_DEFAULT_CONTENT);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to create temporary VXM settings: " + e.getMessage());
                }
                try {
                    FileUtils.copyAll(vxmFile, vxmTempDirectory);
                } catch (IOException e) {
                    System.err.println("Failed to create temporary VXM executable: " + e.getMessage());
                }
            }
            FileUtils.deleteOnExitRecursively(vxmTempDirectory);
            // Visit all files to put them in the deletion queue on application close
            getTempVxmCommandFile();
            getTempVxmVsettingsFile();
            getTempVxmCpnFile();
            getTempVxmInFile();
            getTempVxmPncFile();
            getTempVxmJsonFile();
            getTempVxmSolnFile();
            getTempVxmQslFile();
            getTempVxmEquFile();
            getTempVxmQueFile();
            getTempVxmLocFile();
            getTempVxmSyncFile();
            getTempVxmQlistFile();
        }
        return vxmTempDirectory;
    }

    public static File getTempVxmCommandFile() {
        File vxmFile = new File(getVxmCommand());
        File result = new File(getTempVxmDirectory(), vxmFile.getName());
        result.setExecutable(true);
        return result;
    }

    public static File getTempVxmVsettingsFile() {
        File result = new File(getTempVxmDirectory(), VSETTINGS_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmCpnFile() {
        File result = new File(getTempVxmDirectory(), CPN_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmInFile() {
        File result = new File(getTempVxmDirectory(), IN_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmPncFile() {
        File result = new File(getTempVxmDirectory(), PNC_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmJsonFile() {
        File result = new File(getTempVxmDirectory(), JSON_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmSolnFile() {
        File result = new File(getTempVxmDirectory(), SOLN_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmQslFile() {
        File result = new File(getTempVxmDirectory(), QSL_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmEquFile() {
        File result = new File(getTempVxmDirectory(), EQU_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmQueFile() {
        File result = new File(getTempVxmDirectory(), QUE_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmLocFile() {
        File result = new File(getTempVxmDirectory(), LOC_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmSyncFile() {
        File result = new File(getTempVxmDirectory(), SYNC_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

    public static File getTempVxmQlistFile() {
        File result = new File(getTempVxmDirectory(), QLIST_FILE_NAME);
        result.deleteOnExit();
        return result;
    }

}
