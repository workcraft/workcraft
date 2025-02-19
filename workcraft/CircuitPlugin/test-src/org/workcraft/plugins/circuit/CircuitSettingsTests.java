package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;

class CircuitSettingsTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void circuitSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CircuitSettings";

        Assertions.assertEquals(Config.toString(CircuitSettings.getContactFontSize()),
                framework.getConfigVar(prefix + ".contactFontSize", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getShowZeroDelayNames()),
                framework.getConfigVar(prefix + ".showZeroDelayNames", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getShowContactFunctions()),
                framework.getConfigVar(prefix + ".showContactFunctions", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getShowContactFanout()),
                framework.getConfigVar(prefix + ".showContactFanout", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getBorderWidth()),
                framework.getConfigVar(prefix + ".borderWidth", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getWireWidth()),
                framework.getConfigVar(prefix + ".wireWidth", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getActiveWireColor()),
                framework.getConfigVar(prefix + ".activeWireColor", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getInactiveWireColor()),
                framework.getConfigVar(prefix + ".inactiveWireColor", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getSimplifyStg()),
                framework.getConfigVar(prefix + ".simplifyStg", false));

        // Gate library
        Assertions.assertEquals(Config.toString(CircuitSettings.getGateLibrary()),
                framework.getConfigVar(prefix + ".gateLibrary", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getWaitData()),
                framework.getConfigVar(prefix + ".waitData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getWait0Data()),
                framework.getConfigVar(prefix + ".wait0Data", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getMutexData()),
                framework.getConfigVar(prefix + ".mutexData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getMutexLateData()),
                framework.getConfigVar(prefix + ".mutexLateData", false));

        // Import/export
        Assertions.assertEquals(Config.toString(CircuitSettings.getExportSubstitutionLibrary()),
                framework.getConfigVar(prefix + ".exportSubstitutionLibrary", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getInvertExportSubstitutionRules()),
                framework.getConfigVar(prefix + ".invertExportSubstitutionRules", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getImportSubstitutionLibrary()),
                framework.getConfigVar(prefix + ".importSubstitutionLibrary", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getInvertImportSubstitutionRules()),
                framework.getConfigVar(prefix + ".invertImportSubstitutionRules", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getVerilogAssignDelay()),
                framework.getConfigVar(prefix + ".verilogAssignDelay", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getVerilogTimescale()),
                framework.getConfigVar(prefix + ".verilogTimescale", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getBusSuffix()),
                framework.getConfigVar(prefix + ".busSuffix", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getDissolveSingletonBus()),
                framework.getConfigVar(prefix + ".dissolveSingletonBus", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getAcceptInoutPort()),
                framework.getConfigVar(prefix + ".acceptInoutPort", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getModuleFilePattern()),
                framework.getConfigVar(prefix + ".moduleFilePattern", false));

        // Reset
        Assertions.assertEquals(Config.toString(CircuitSettings.getResetActiveHighPort()),
                framework.getConfigVar(prefix + ".resetActiveHighPort", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getResetActiveLowPort()),
                framework.getConfigVar(prefix + ".resetActiveLowPort", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getInitLowMappingRules()),
                framework.getConfigVar(prefix + ".initLowMappingRules", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getInitHighMappingRules()),
                framework.getConfigVar(prefix + ".initHighMappingRules", false));

        // Scan
        Assertions.assertEquals(Config.toString(CircuitSettings.getTbufData()),
                framework.getConfigVar(prefix + ".tbufData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getTinvData()),
                framework.getConfigVar(prefix + ".tinvData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getTestInstancePrefix()),
                framework.getConfigVar(prefix + ".testInstancePrefix", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getUseTestPathBreaker()),
                framework.getConfigVar(prefix + ".useTestPathBreaker", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getScanSuffix()),
                framework.getConfigVar(prefix + ".scanSuffix", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getScaninData()),
                framework.getConfigVar(prefix + ".scaninData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getScanoutData()),
                framework.getConfigVar(prefix + ".scanoutData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getScanenData()),
                framework.getConfigVar(prefix + ".scanenData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getScanckData()),
                framework.getConfigVar(prefix + ".scanckData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getScantmData()),
                framework.getConfigVar(prefix + ".scantmData", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getUseIndividualScan()),
                framework.getConfigVar(prefix + ".useIndividualScan", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getScanoutBufferingStyle()),
                framework.getConfigVar(prefix + ".scanoutBufferingStyle", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getUseScanInitialisation()),
                framework.getConfigVar(prefix + ".useScanInitialisation", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getInitialisationInverterInstancePrefix()),
                framework.getConfigVar(prefix + ".initialisationInverterInstancePrefix", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getAuxiliaryPortRegex()),
                framework.getConfigVar(prefix + ".auxiliaryPortRegex", false));

        // Forks
        Assertions.assertEquals(Config.toString(CircuitSettings.getForkHighFanout()),
                framework.getConfigVar(prefix + ".forkHighFanout", false));

        Assertions.assertEquals(Config.toString(CircuitSettings.getForkBufferPattern()),
                framework.getConfigVar(prefix + ".forkBufferPattern", false));
    }

}
