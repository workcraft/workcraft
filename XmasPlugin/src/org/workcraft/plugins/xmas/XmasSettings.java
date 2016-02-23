package org.workcraft.plugins.xmas;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.util.FileUtils;

public class XmasSettings implements Settings {
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

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
    private static final String prefix = "XmasSettings";

    private static final String keyShowContacts  = prefix + ".showContacts";
    private static final String keyBorderWidth = prefix + ".borderWidth";
    private static final String keyWireWidth  = prefix + ".wireWidth";
    private static final String keyVxmCommand  = prefix + ".vxmCommand";

    private static final boolean defaultShowContacts = false;
    private static final double defaultBorderWidth = 0.06;
    private static final double defaultWireWidth = 0.06;
    private static final String defaultVxmCommand = DesktopApi.getOs().isWindows() ? "tools\\vxm\\vxm.exe" : "tools/vxm/vxm";

    private static boolean showContacts = defaultShowContacts;
    private static double borderWidth = defaultBorderWidth;
    private static double wireWidth = defaultWireWidth;
    private static String vxmCommand = defaultVxmCommand;

    private static File vxmTempDirectory = null;

    public XmasSettings() {
        properties.add(new PropertyDeclaration<XmasSettings, Boolean>(
                this, "Show contacts", Boolean.class, true, false, false) {
            protected void setter(XmasSettings object, Boolean value) {
                setShowContacts(value);
            }
            protected Boolean getter(XmasSettings object) {
                return getShowContacts();
            }
        });

        properties.add(new PropertyDeclaration<XmasSettings, Double>(
                this, "Border width", Double.class, true, false, false) {
            protected void setter(XmasSettings object, Double value) {
                XmasSettings.setBorderWidth(value);
            }
            protected Double getter(XmasSettings object) {
                return XmasSettings.getBorderWidth();
            }
        });

        properties.add(new PropertyDeclaration<XmasSettings, Double>(
                this, "Wire width", Double.class, true, false, false) {
            protected void setter(XmasSettings object, Double value) {
                XmasSettings.setWireWidth(value);
            }
            protected Double getter(XmasSettings object) {
                return XmasSettings.getWireWidth();
            }
        });

        properties.add(new PropertyDeclaration<XmasSettings, String>(
                this, "VXM cmmand", String.class, true, false, false) {
            protected void setter(XmasSettings object, String value) {
                XmasSettings.setVxmCommand(value);
            }
            protected String getter(XmasSettings object) {
                return XmasSettings.getVxmCommand();
            }
        });
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
    public String getSection() {
        return "Models";
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
            Framework framework = Framework.getInstance();
            vxmTempDirectory = FileUtils.createTempDirectory("vxm-");
            File vxmFile = new File(getVxmCommand());
            if (framework.checkFile(vxmFile, "VXM access error")) {
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
