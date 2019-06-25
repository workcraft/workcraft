package org.workcraft.plugins.circuit.utils;

import org.workcraft.plugins.circuit.verilog.*;
import org.workcraft.utils.LogUtils;

import java.util.*;
import java.util.stream.Collectors;

public class VerilogUtils {

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
            Set<VerilogModule> instantiatedModules = result.get(module);
            if (instantiatedModules == null) {
                instantiatedModules = new HashSet<>();
                result.put(module, instantiatedModules);
            }
            for (VerilogInstance instance : module.instances) {
                VerilogModule instantiatedModule = nameToModuleMap.get(instance.moduleName);
                if (instantiatedModule != null) {
                    instantiatedModules.add(instantiatedModule);
                }
            }
        }
        return result;
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

    public static void printModule(VerilogModule verilogModule) {
        String portNames = verilogModule.ports.stream()
                .map(verilogPort -> verilogPort.name)
                .collect(Collectors.joining(", "));
        LogUtils.logMessage("module " + verilogModule.name + " (" + portNames + ");");
        for (VerilogPort verilogPort : verilogModule.ports) {
            LogUtils.logMessage("    " + verilogPort.type + " " + ((verilogPort.range == null) ? "" : verilogPort.range + " ") + verilogPort.name + ";");
        }
        for (VerilogAssign verilogAssign : verilogModule.assigns) {
            LogUtils.logMessage("    assign " + verilogAssign.name + " = " + verilogAssign.formula + ";");
        }

        for (VerilogInstance verilogInstance : verilogModule.instances) {
            String instanceName = (verilogInstance.name == null) ? "" : verilogInstance.name;
            String pinNames = verilogInstance.connections.stream()
                    .map(verilogConnection -> getConnectionString(verilogConnection))
                    .collect(Collectors.joining(", "));
            LogUtils.logMessage("    " + verilogInstance.moduleName + " " + instanceName + " (" + pinNames + ");");
        }
        LogUtils.logMessage("endmodule\n");
    }

    public static String getConnectionString(VerilogConnection verilogConnection) {
        String result = verilogConnection.netName + ((verilogConnection.netIndex == null) ? "" : "[" + verilogConnection.netIndex + "]");
        if (verilogConnection.name != null) {
            result = "." + verilogConnection.name + "(" + result + ")";
        }
        return result;
    }

}
