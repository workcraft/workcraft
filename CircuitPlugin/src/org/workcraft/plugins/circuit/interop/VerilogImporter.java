/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.circuit.interop;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.CircuitModelDescriptor;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.javacc.ParseException;
import org.workcraft.plugins.circuit.javacc.VerilogParser;
import org.workcraft.plugins.circuit.javacc.VerilogParser.Module;
import org.workcraft.workspace.ModelEntry;

public class VerilogImporter implements Importer {

	@Override
	public boolean accept(File file) {
		return file.getName().endsWith(".v");
	}

	@Override
	public String getDescription() {
		return "Verilog (.v)";
	}

	@Override
	public ModelEntry importFrom(InputStream in) throws DeserialisationException {
		return new ModelEntry(new CircuitModelDescriptor(), importCircuit(in));
	}

	public Circuit importCircuit(InputStream in) throws DeserialisationException {
		try {
			VerilogParser parser = new VerilogParser(in);
			HashMap<String, Module> modules = getModuleMap(parser.parseCircuit());
			HashSet<VerilogParser.Module> topModules = getTopModule(modules);
			if (topModules.size() == 0) {
				throw new RuntimeException("No top module found.");
			}
			if (topModules.size() > 1) {
				throw new RuntimeException("Too many top modules found.");
			}
			printDebugInfo(topModules);
			Module topModule = topModules.iterator().next();
			Circuit circuit = generateCircuit(topModule, modules);
			return circuit;
		} catch (FormatException e) {
			throw new DeserialisationException(e);
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}
	}

	private HashSet<VerilogParser.Module> getTopModule(HashMap<String, Module> modules) {
		HashSet<VerilogParser.Module> result = new HashSet<>(modules.values());
		for (VerilogParser.Module module: modules.values()) {
			if (module.instances.isEmpty()) {
				result.remove(module);
			}
			for (VerilogParser.Instance instance: module.instances) {
				if (instance.moduleName == null) continue;
				result.remove(modules.get(instance.moduleName));
			}
		}
		return result;
	}

	private void printDebugInfo(Collection<VerilogParser.Module> modules) {
		for (VerilogParser.Module module: modules) {
			System.out.println("Module: '" + module.name + "'");
			for (VerilogParser.Port port: module.ports) {
				System.out.println("  Port: '" + port.name + "' (" + port.type +")");
			}
			for (VerilogParser.Instance instance: module.instances) {
				System.out.println("  Instance: '" + instance.name + "' (" + instance.moduleName +")");
				for (VerilogParser.Connection connection: instance.connections) {
					System.out.println("    Connection: '" + connection.name + "' (" + connection.netName +")");
				}
			}
		}
	}

	class Wire {
		public FunctionContact source = null;
		public HashSet<FunctionContact> sinks = new HashSet<>();
	}

	private Circuit generateCircuit(Module topModule, HashMap<String, Module> modules) {
		Circuit circuit = new Circuit();
		HashMap<String, Wire> wires = new HashMap<>();
		for (VerilogParser.Port verilogPort: topModule.ports) {
			FunctionContact contact = new FunctionContact();
			Wire wire = new Wire();
			if ("input".equals(verilogPort.type)) {
				contact.setIOType(IOType.INPUT);
				wire.source = contact;
			}
			if ("output".equals(verilogPort.type)) {
				contact.setIOType(IOType.OUTPUT);
				wire.sinks.add(contact);
			}
			wires.put(verilogPort.name, wire);
			circuit.setName(contact, verilogPort.name);
			circuit.add(contact);
		}
		for (VerilogParser.Instance verilogInstance: topModule.instances) {
			FunctionComponent component = new FunctionComponent();
			component.setModule(verilogInstance.moduleName);
			circuit.setName(component, verilogInstance.name);
			circuit.add(component);
			VerilogParser.Module module = modules.get(verilogInstance.moduleName);
			HashMap<String, VerilogParser.Port> instancePorts = getModulePortMap(module);
			for (VerilogParser.Connection verilogConnection: verilogInstance.connections) {
				FunctionContact contact = new FunctionContact();
				VerilogParser.Port verilogPort = instancePorts.get(verilogConnection.name);
				Wire wire = wires.get(verilogConnection.netName);
				if (wire == null) {
					wire = new Wire();
					wires.put(verilogConnection.netName, wire);
				}
				if ((verilogPort != null) && ("input".equals(verilogPort.type))) {
					contact.setIOType(IOType.INPUT);
					wire.sinks.add(contact);
				} else {
					contact.setIOType(IOType.OUTPUT);
					wire.source = contact;
				}
				component.add(contact);
				circuit.setName(contact, verilogConnection.name);
			}
		}
		for (Wire wire: wires.values()) {
			if (wire.source == null) continue;
			for (FunctionContact sink: wire.sinks) {
				try {
					circuit.connect(wire.source, sink);
				} catch (InvalidConnectionException e) {
				}
			}
		}
		return circuit;
	}

	private HashMap<String, VerilogParser.Module> getModuleMap(List<VerilogParser.Module> modules) throws ParseException {
		HashMap<String, VerilogParser.Module> result = new HashMap<>();
		for (VerilogParser.Module module: modules) {
			if ((module == null) || (module.name == null)) continue;
			result.put(module.name, module);
		}
		return result;
	}

	private HashMap<String, VerilogParser.Port> getModulePortMap(VerilogParser.Module module) {
		HashMap<String, VerilogParser.Port> result = new HashMap<>();
		if (module != null) {
			for (VerilogParser.Port port: module.ports) {
				result.put(port.name, port);
			}
		}
		return result;
	}

}
