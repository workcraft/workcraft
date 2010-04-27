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

package org.workcraft.parsers.breeze.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.parsers.breeze.BreezeDefinition;
import org.workcraft.parsers.breeze.BreezeFactory;
import org.workcraft.parsers.breeze.BreezeInstance;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.BreezePartReference;
import org.workcraft.parsers.breeze.EmptyParameterScope;
import org.workcraft.parsers.breeze.ParameterValueList;

public class BreezePart implements BreezeDefinition
{
	private final String name;
	private final List<PortDeclaration> ports;
	private final List<ChannelDeclaration> channels;
	private final List<RawBreezePartReference> parts;

	public BreezePart(String name, List<PortDeclaration> ports, List<ChannelDeclaration> channels, List<RawBreezePartReference> parts)
	{
		this.ports = Collections.unmodifiableList(ports);
		this.channels = Collections.unmodifiableList(channels);
		this.parts = Collections.unmodifiableList(parts);
		this.name = name;
	}

	private static List<BreezePartReference> resolve(BreezeLibrary library, List<RawBreezePartReference> parts) {
		ArrayList<BreezePartReference> result = new ArrayList<BreezePartReference>();
		for(RawBreezePartReference ref : parts)
		{
			result.add(new BreezePartReference(library, ref));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Port> BreezeInstance<Port> instantiate(BreezeLibrary library, BreezeFactory<Port> factory, ParameterValueList parameters)
	{
		ensureEmpty(parameters);

		List<Port> [] connections = new List[getChannels().size()];
		for(int i=0;i<getChannels().size();i++)
			connections[i] = new ArrayList<Port>();

		ArrayList<BreezeInstance<Port>> contained = new ArrayList<BreezeInstance<Port>>();
		for(BreezePartReference part : resolve(library,getParts()))
		{
			BreezeInstance<Port> instance = part.definition().instantiate(library, factory, part.parameters());
			int referencedIndex = 0;
			for(List<Integer> indices : part.connections())
			{
				for(Integer index : indices)
					connections[index-1].add(instance.ports().get(referencedIndex++));
			}
			contained.add(instance);
		}

		final List<Port> externalChannels = new ArrayList<Port>();
		int portChannels = getPortCount();
		for(int i=0;i<portChannels;i++)
		{
			if(connections[i].size()!=1)
				throw new RuntimeException("A port should be connected to exactly one internal port");
			externalChannels.add(connections[i].get(0));
		}

		for(int i=portChannels;i<getChannels().size();i++)
		{
			if(connections[i].size()!=2)
				throw new RuntimeException("A channel should connect exactly two internal ports");
			factory.connect(connections[i].get(0), connections[i].get(1));
		}

		return new BreezeInstance<Port>()
		{
			@Override public List<Port> ports() {
				return externalChannels;
			}
		};
	}

	private void ensureEmpty(ParameterValueList parameters) {
		if(parameters.size() != 0)
			throw new NotSupportedException("Breeze Parts with parameters are not supported");
	}

	private int getPortCount() {
		int count = 0;
		for(PortDeclaration portDeclaration : getPorts())
		{
			count += portDeclaration.count().evaluate(EmptyParameterScope.instance());
		}
		return count;
	}

	public String getName() {
		return name;
	}

	public List<PortDeclaration> getPorts() {
		return ports;
	}

	public List<ChannelDeclaration> getChannels() {
		return channels;
	}

	public List<RawBreezePartReference> getParts() {
		return parts;
	}
}

