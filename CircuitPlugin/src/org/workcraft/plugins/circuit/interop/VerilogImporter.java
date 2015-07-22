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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitModelDescriptor;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.javacc.GenlibParser;
import org.workcraft.plugins.circuit.javacc.ParseException;
import org.workcraft.plugins.circuit.javacc.VerilogParser;
import org.workcraft.plugins.circuit.verilog.Instance;
import org.workcraft.plugins.circuit.verilog.Module;
import org.workcraft.plugins.circuit.verilog.Pin;
import org.workcraft.plugins.circuit.verilog.Port;
import org.workcraft.workspace.ModelEntry;


public class VerilogImporter implements Importer {

	private class Wire {
		public FunctionContact source = null;
		public HashSet<FunctionContact> sinks = new HashSet<>();
	}

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
			HashSet<Module> topModules = getTopModule(modules);
			if (topModules.size() == 0) {
				throw new RuntimeException("No top module found.");
			}
			if (topModules.size() > 1) {
				throw new RuntimeException("Too many top modules found.");
			}
			//printDebugInfo(topModules);
			Module topModule = topModules.iterator().next();
			Circuit circuit = createCircuit(topModule, modules);
			return circuit;
		} catch (FormatException e) {
			throw new DeserialisationException(e);
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}
	}

	private HashSet<Module> getTopModule(HashMap<String, Module> modules) {
		HashSet<Module> result = new HashSet<>(modules.values());
		for (Module module: modules.values()) {
			if (module.instances.isEmpty()) {
				result.remove(module);
			}
			for (Instance instance: module.instances) {
				if (instance.moduleName == null) continue;
				result.remove(modules.get(instance.moduleName));
			}
		}
		return result;
	}

	private void printDebugInfo(Collection<Module> modules) {
		for (Module module: modules) {
			System.out.println("Module: '" + module.name + "'");
			for (Port port: module.ports) {
				System.out.println("  Port: '" + port.name + "' (" + port.type +")");
			}
			for (Instance instance: module.instances) {
				System.out.println("  Instance: '" + instance.name + "' (" + instance.moduleName +")");
				for (Pin connection: instance.connections) {
					System.out.println("    Connection: '" + connection.name + "' (" + connection.netName +")");
				}
			}
		}
	}

	private Circuit createCircuit(Module topModule, HashMap<String, Module> modules) {
		Circuit circuit = new Circuit();
		Library library = null;
		try {
			InputStream genlibInputStream = new FileInputStream(CircuitSettings.getGateLibrary());
			library = new GenlibParser(genlibInputStream).parseGenlib();
		} catch (FileNotFoundException e) {
		} catch (ParseException e) {
		}

		HashMap<String, Wire> wires = createPorts(circuit, topModule);

		for (Instance verilogInstance: topModule.instances) {
			Gate gate = (library == null ? null : library.get(verilogInstance.moduleName));
			if (gate != null) {
				createGateInstance(circuit, verilogInstance, wires, gate);
			} else {
				createBoxInstance(circuit, verilogInstance, wires, modules);
			}
		}
		createConnections(circuit, wires);
		return circuit;
	}

	private void createGateInstance(Circuit circuit, Instance verilogInstance,
			HashMap<String, Wire> wires, Gate gate) {
		FunctionComponent component = GenlibUtils.instantiateGate(gate, verilogInstance.name, circuit);
		for (Pin verilogPin: verilogInstance.connections) {
			Wire wire = wires.get(verilogPin.netName);
			if (wire == null) {
				wire = new Wire();
				wires.put(verilogPin.netName, wire);
			}
			Node node = circuit.getNodeByReference(component, verilogPin.name);
			if (node instanceof FunctionContact) {
				FunctionContact contact = (FunctionContact)node;
				if (contact.isInput()) {
					wire.sinks.add(contact);
				} else {
					wire.source = contact;
				}
			}
		}
	}

	private void createBoxInstance(Circuit circuit, Instance verilogInstance,
			HashMap<String, Wire> wires, HashMap<String, Module> modules) {
		final FunctionComponent component = new FunctionComponent();
		component.setModule(verilogInstance.moduleName);
		circuit.add(component);
		circuit.setName(component, verilogInstance.name);
		Module module = modules.get(verilogInstance.moduleName);
		HashMap<String, Port> instancePorts = getModulePortMap(module);
		for (Pin verilogPin: verilogInstance.connections) {
			Port verilogPort = instancePorts.get(verilogPin.name);
			Wire wire = wires.get(verilogPin.netName);
			if (wire == null) {
				wire = new Wire();
				wires.put(verilogPin.netName, wire);
			}
			FunctionContact contact = new FunctionContact();
			if ((verilogPort != null) && ("input".equals(verilogPort.type))) {
				contact.setIOType(IOType.INPUT);
				wire.sinks.add(contact);
			} else {
				contact.setIOType(IOType.OUTPUT);
				wire.source = contact;
			}
			component.add(contact);
			if (verilogPin.name != null) {
				circuit.setName(contact, verilogPin.name);
			}
		}
	}

	private HashMap<String, Wire> createPorts(Circuit circuit, Module module) {
		HashMap<String, Wire> wires = new HashMap<>();
		for (Port verilogPort: module.ports) {
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
		return wires;
	}

	private void createConnections(Circuit circuit, HashMap<String, Wire> wires) {
		for (Wire wire: wires.values()) {
			if (wire.source == null) continue;
			for (FunctionContact sink: wire.sinks) {
				try {
					circuit.connect(wire.source, sink);
				} catch (InvalidConnectionException e) {
				}
			}
		}
	}

	private HashMap<String, Module> getModuleMap(List<Module> modules) throws ParseException {
		HashMap<String, Module> result = new HashMap<>();
		for (Module module: modules) {
			if ((module == null) || (module.name == null)) continue;
			result.put(module.name, module);
		}
		return result;
	}

	private HashMap<String, Port> getModulePortMap(Module module) {
		HashMap<String, Port> result = new HashMap<>();
		if (module != null) {
			for (Port port: module.ports) {
				result.put(port.name, port);
			}
		}
		return result;
	}

}
