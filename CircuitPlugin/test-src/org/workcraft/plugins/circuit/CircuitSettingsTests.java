package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

public class CircuitSettingsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void circuitSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CircuitSettings";

        Assert.assertEquals(Config.toString(CircuitSettings.getShowContacts()),
                framework.getConfigVar(prefix + ".showContacts", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getContactFontSize()),
                framework.getConfigVar(prefix + ".contactFontSize", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getFunctionFontSize()),
                framework.getConfigVar(prefix + ".functionFontSize", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getShowZeroDelayNames()),
                framework.getConfigVar(prefix + ".showZeroDelayNames", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getBorderWidth()),
                framework.getConfigVar(prefix + ".borderWidth", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getWireWidth()),
                framework.getConfigVar(prefix + ".wireWidth", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getActiveWireColor()),
                framework.getConfigVar(prefix + ".activeWireColor", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getInactiveWireColor()),
                framework.getConfigVar(prefix + ".inactiveWireColor", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getSimplifyStg()),
                framework.getConfigVar(prefix + ".simplifyStg", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getGateLibrary()),
                framework.getConfigVar(prefix + ".gateLibrary", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getExportSubstitutionLibrary()),
                framework.getConfigVar(prefix + ".exportSubstitutionLibrary", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getInvertExportSubstitutionRules()),
                framework.getConfigVar(prefix + ".invertExportSubstitutionRules", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getImportSubstitutionLibrary()),
                framework.getConfigVar(prefix + ".importSubstitutionLibrary", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getInvertImportSubstitutionRules()),
                framework.getConfigVar(prefix + ".invertImportSubstitutionRules", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getBufData()),
                framework.getConfigVar(prefix + ".bufData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getAndData()),
                framework.getConfigVar(prefix + ".andData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getOrData()),
                framework.getConfigVar(prefix + ".orData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getNandData()),
                framework.getConfigVar(prefix + ".nandData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getNorData()),
                framework.getConfigVar(prefix + ".norData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getNandbData()),
                framework.getConfigVar(prefix + ".nandbData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getNorbData()),
                framework.getConfigVar(prefix + ".norbData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getMutexData()),
                framework.getConfigVar(prefix + ".mutexData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getBusSuffix()),
                framework.getConfigVar(prefix + ".busSuffix", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getResetPort()),
                framework.getConfigVar(prefix + ".resetPort", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getSetPin()),
                framework.getConfigVar(prefix + ".setPin", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getClearPin()),
                framework.getConfigVar(prefix + ".clearPin", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getTbufData()),
                framework.getConfigVar(prefix + ".tbufData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getTinvData()),
                framework.getConfigVar(prefix + ".tinvData", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getScanSuffix()),
                framework.getConfigVar(prefix + ".scanSuffix", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getScaninPortPin()),
                framework.getConfigVar(prefix + ".scaninPortPin", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getScanoutPortPin()),
                framework.getConfigVar(prefix + ".scanoutPortPin", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getScanckPortPin()),
                framework.getConfigVar(prefix + ".scanckPortPin", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getScanenPortPin()),
                framework.getConfigVar(prefix + ".scanenPortPin", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getScantmPortPin()),
                framework.getConfigVar(prefix + ".scantmPortPin", false));

        Assert.assertEquals(Config.toString(CircuitSettings.getVerilogAssignDelay()),
                framework.getConfigVar(prefix + ".verilogAssignDelay", false));
    }

}
