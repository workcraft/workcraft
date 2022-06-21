package org.workcraft.plugins.circuit.utils;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.jj.verilog.VerilogParser;
import org.workcraft.plugins.circuit.verilog.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.FileFilters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class VerilogUtils {

    public static final String BUS_INDEX_PLACEHOLDER = "$";

    private static final String PRIMITIVE_GATE_INPUT_PREFIX = "i";
    private static final String PRIMITIVE_GATE_OUTPUT_NAME = "o";

    private static final Pattern NUMBER_DELAY_PATTERN = Pattern.compile("\\d*\\.?\\d+");
    private static final Pattern STRING_DELAY_PATTERN = Pattern.compile("^\\(.*\\)$");

    private static final Map<String, Pair<Boolean, String>> PRIMITIVE_OPERATOR_MAP = new HashMap<>();
    static {
        PRIMITIVE_OPERATOR_MAP.put("buf", Pair.of(true, ""));
        PRIMITIVE_OPERATOR_MAP.put("not", Pair.of(false, ""));
        PRIMITIVE_OPERATOR_MAP.put("and", Pair.of(true, "*"));
        PRIMITIVE_OPERATOR_MAP.put("nand", Pair.of(false, "*"));
        PRIMITIVE_OPERATOR_MAP.put("or", Pair.of(true, "+"));
        PRIMITIVE_OPERATOR_MAP.put("nor", Pair.of(false, "+"));
        PRIMITIVE_OPERATOR_MAP.put("xnor", Pair.of(false, "^"));
    }

    private VerilogUtils() {
    }

    public static Map<VerilogModule, String> getModuleToFileMap(Collection<VerilogModule> modules) {
        Map<VerilogModule, String> result = new HashMap<>();
        for (VerilogModule module : modules) {
            result.put(module, module.name + FileFilters.DOCUMENT_EXTENSION);
        }
        return result;
    }

    public static Set<VerilogModule> getDescendantModules(VerilogModule module, Collection<VerilogModule> modules) {
        return getModuleToDescendantsMap(modules).get(module);
    }

    public static Map<VerilogModule, Set<VerilogModule>> getModuleToDescendantsMap(Collection<VerilogModule> modules) {
        Map<VerilogModule, Set<VerilogModule>> result = getModuleToChildrenMap(modules);
        for (VerilogModule module : modules) {
            Set<VerilogModule> children = result.get(module);
            boolean done = false;
            while ((children != null) && !done) {
                done = true;
                Set<VerilogModule> tmpChildren = new HashSet<>(children);
                for (VerilogModule child : tmpChildren) {
                    Set<VerilogModule> descendant = result.get(child);
                    if (descendant == null) continue;
                    done &= !children.addAll(descendant);
                }
            }
        }
        return result;
    }

    public static Map<VerilogModule, Set<VerilogModule>> getModuleToChildrenMap(Collection<VerilogModule> modules) {
        Map<VerilogModule, Set<VerilogModule>> result = new HashMap<>();
        HashMap<String, VerilogModule> nameToModuleMap = getNameToModuleMap(modules);
        for (VerilogModule module : modules) {
            Set<VerilogModule> instantiatedModules = result.computeIfAbsent(module, s -> new HashSet<>());
            for (VerilogInstance instance : module.instances) {
                VerilogModule instantiatedModule = nameToModuleMap.get(instance.moduleName);
                if (instantiatedModule != null) {
                    instantiatedModules.add(instantiatedModule);
                }
            }
        }
        return result;
    }

    public static VerilogModule getTopModule(Collection<VerilogModule> verilogModules)
            throws DeserialisationException {

        Collection<VerilogModule> topVerilogModules = VerilogUtils.getTopModules(verilogModules);
        if (topVerilogModules.isEmpty()) {
            throw new DeserialisationException("No top module found.");
        }
        if (topVerilogModules.size() > 1) {
            throw new DeserialisationException("More than one top module are found.");
        }
        return topVerilogModules.iterator().next();
    }

    public static Collection<VerilogModule> getTopModules(Collection<VerilogModule> verilogModules) {
        HashMap<String, VerilogModule> nameToModuleMap = getNameToModuleMap(verilogModules);
        HashSet<VerilogModule> result = new HashSet<>(verilogModules);
        if (verilogModules.size() > 1) {
            for (VerilogModule verilogModule : verilogModules) {
                if (verilogModule.isEmpty()) {
                    result.remove(verilogModule);
                }
                for (VerilogInstance verilogInstance : verilogModule.instances) {
                    result.remove(nameToModuleMap.get(verilogInstance.moduleName));
                }
            }
        }
        return result;
    }

    private static HashMap<String, VerilogModule> getNameToModuleMap(Collection<VerilogModule> verilogModules) {
        HashMap<String, VerilogModule> nameToModuleMap = new HashMap<>();
        for (VerilogModule verilogModule : verilogModules) {
            if ((verilogModule != null) && (verilogModule.name != null)) {
                nameToModuleMap.put(verilogModule.name, verilogModule);
            }
        }
        return nameToModuleMap;
    }

    public static VerilogModule importTopVerilogModule(File file) {
        try (FileInputStream is = new FileInputStream(file)) {
            return getTopModule(importVerilogModules(is));
        } catch (IOException | DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Collection<VerilogModule> importVerilogModules(InputStream in)
            throws DeserialisationException {

        Collection<VerilogModule> result;
        try {
            VerilogParser parser = new VerilogParser(in);
            if (DebugCommonSettings.getParserTracing()) {
                parser.enable_tracing();
            } else {
                parser.disable_tracing();
            }
            result = parser.parseCircuit();
        } catch (FormatException | org.workcraft.plugins.circuit.jj.verilog.ParseException e) {
            throw new DeserialisationException(e);
        }
        if (DebugCommonSettings.getVerboseImport()) {
            LogUtils.logInfo("Parsed Verilog modules");
            for (VerilogModule verilogModule : result) {
                if (!verilogModule.isEmpty()) {
                    printModule(verilogModule);
                }
            }
        }
        return result;
    }

    public static void printModule(VerilogModule verilogModule) {
        String portNames = verilogModule.ports.stream()
                .map(verilogPort -> verilogPort.name)
                .collect(Collectors.joining(", "));

        LogUtils.logMessage("module " + verilogModule.name + " (" + portNames + ");");
        for (VerilogPort verilogPort : verilogModule.ports) {
            LogUtils.logMessage("    " + verilogPort.type + " "
                    + ((verilogPort.range == null) ? "" : verilogPort.range + " ")
                    + verilogPort.name + ";");
        }
        for (VerilogAssign verilogAssign : verilogModule.assigns) {
            String wireName = getNetBusIndexName(verilogAssign.net);
            if (wireName != null) {
                LogUtils.logMessage("    assign " + wireName + " = " + verilogAssign.formula + ";");
            }
        }

        for (VerilogInstance verilogInstance : verilogModule.instances) {
            String instanceName = (verilogInstance.name == null) ? "" : verilogInstance.name;
            String pinNames = verilogInstance.connections.stream()
                    .map(VerilogUtils::getConnectionString)
                    .collect(Collectors.joining(", "));

            LogUtils.logMessage("    " + verilogInstance.moduleName + " " + instanceName + " (" + pinNames + ");");
        }
        LogUtils.logMessage("endmodule\n");
    }

    private static String getConnectionString(VerilogConnection verilogConnection) {
        String netNames = getNetsString(verilogConnection.nets);
        return verilogConnection.name == null ? netNames : "." + verilogConnection.name + "(" + netNames + ")";
    }

    private static String getNetsString(List<VerilogNet> verilogNets) {
        List<String> netNames = new ArrayList<>();
        for (VerilogNet verilogNet : verilogNets) {
            netNames.add(getNetBusIndexName(verilogNet));
        }
        String s = String.join(", ", netNames);
        return (verilogNets.size() > 1) ? "{" + s + "}" : s;
    }

    public static String getNetBusIndexName(VerilogNet net) {
        return net == null ? null : net.getName() + (net.getIndex() == null ? "" : "[" + net.getIndex() + "]");
    }

    public static String getNetBusSuffixName(VerilogNet net) {
        return (net == null) || (net.getName() == null) ? null : net.getName() + getBusSuffix(net.getIndex());
    }

    public static String getSignalWithBusSuffix(String name, Integer index) {
        return name + getBusSuffix(index);
    }

    private static String getBusSuffix(Integer index) {
        return (index == null) ? "" : getProcessedBusSuffix().replace(BUS_INDEX_PLACEHOLDER, Integer.toString(index));
    }

    public static String getFormulaWithBusSuffixNames(String verilogFormula) {
        String busSuffix = getProcessedBusSuffix();
        String busSuffixReplacement = busSuffix.replace(BUS_INDEX_PLACEHOLDER, "$1");
        return verilogFormula.replaceAll("\\[(\\d+)]", busSuffixReplacement);
    }

    public static Pattern getBusSignalPattern() {
        String busSuffix = getProcessedBusSuffix();
        return Pattern.compile("(.+)" + busSuffix.replace(BUS_INDEX_PLACEHOLDER, "(\\d+)"));
    }

    private static String getProcessedBusSuffix() {
        String result = CircuitSettings.getBusSuffix();
        if (result == null) {
            result = BUS_INDEX_PLACEHOLDER;
        }
        if (!result.contains(BUS_INDEX_PLACEHOLDER)) {
            result += BUS_INDEX_PLACEHOLDER;
        }
        return result;
    }

    public static Set<String> getUndefinedModules(Collection<VerilogModule> verilogModules) {
        Set<String> result = new HashSet<>();
        Set<String> moduleNames = verilogModules.stream()
                .map(verilogModule -> verilogModule.name)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (VerilogModule verilogModule : verilogModules) {
            result.addAll(getUndefinedModules(verilogModule.instances, moduleNames));
        }
        return result;
    }

    private static Set<String> getUndefinedModules(Collection<VerilogInstance> verilogInstances, Set<String> moduleNames) {
        Set<String> result = new HashSet<>();
        Map<String, SubstitutionRule> substitutionRules = LibraryManager.getImportSubstitutionRules();
        for (VerilogInstance verilogInstance : verilogInstances) {
            String moduleName = verilogInstance.moduleName;
            SubstitutionRule substitutionRule = substitutionRules.get(moduleName);
            if (substitutionRule != null) {
                moduleName = substitutionRule.newName;
            }
            if (isUndefinedModule(moduleName) && !moduleNames.contains(moduleName)) {
                result.add(moduleName);
            }
        }
        return result;
    }

    public static boolean isUndefinedModule(String moduleName) {
        return !isPrimitiveGate(moduleName)
                && !isWaitInstance(moduleName)
                && !isMutexInstance(moduleName)
                && !isLibraryGate(moduleName);
    }

    public static boolean isPrimitiveGate(String moduleName) {
        return PRIMITIVE_OPERATOR_MAP.containsKey(moduleName);
    }

    public static String getPrimitiveOperator(String moduleName) {
        return PRIMITIVE_OPERATOR_MAP.getOrDefault(moduleName, Pair.of(null, null)).getSecond();
    }

    public static Boolean getPrimitivePolarity(String moduleName) {
        return PRIMITIVE_OPERATOR_MAP.getOrDefault(moduleName, Pair.of(null, null)).getFirst();
    }

    public static String getPrimitiveGatePinName(int index) {
        if (index == 0) {
            return PRIMITIVE_GATE_OUTPUT_NAME;
        } else {
            return PRIMITIVE_GATE_INPUT_PREFIX + index;
        }
    }

    public static boolean isWaitInstance(String moduleName) {
        return ArbitrationUtils.getWaitModuleNames().contains(moduleName);
    }

    public static boolean isMutexInstance(String moduleName) {
        return ArbitrationUtils.getMutexModuleNames().contains(moduleName);
    }

    public static boolean isLibraryGate(String moduleName) {
        Library library = LibraryManager.getLibrary();
        return library.get(moduleName) != null;
    }

    public static boolean checkAssignDelay(String value) {
        return value.isEmpty()
                || NUMBER_DELAY_PATTERN.matcher(value).matches()
                || STRING_DELAY_PATTERN.matcher(value).matches();
    }

    public static String getAssignDelayHelp() {
        return "Delay for Verilog assign statement must be one of these:\n" +
                PropertyHelper.BULLET_PREFIX + "empty string\n" +
                PropertyHelper.BULLET_PREFIX + "an integer or floating-point number\n" +
                PropertyHelper.BULLET_PREFIX + "a string in parenthesis, e.g. `(1ps * $urandom_range(10, 20))`";
    }

}
