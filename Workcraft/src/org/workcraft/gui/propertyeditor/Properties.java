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

package org.workcraft.gui.propertyeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public interface Properties {
	public Collection<PropertyDescriptor> getDescriptors();

	public class Mix implements Properties {
		private LinkedList<PropertyDescriptor> descriptors = new LinkedList<PropertyDescriptor>();

		public Mix(PropertyDescriptor... descriptors) {
			for (PropertyDescriptor descriptor : descriptors)
				this.descriptors.add(descriptor);
		}

		@Override
		public Collection<PropertyDescriptor> getDescriptors() {
			return descriptors;
		}

		public void add (PropertyDescriptor descriptor) {
			if (descriptor != null)
				descriptors.add(descriptor);
		}

		public void add (Properties properties) {
			if (properties != null)
				for (PropertyDescriptor desc : properties.getDescriptors())
					descriptors.add(desc);
		}

		public boolean isEmpty() {
			return descriptors.isEmpty();
		}

		public static Mix from (PropertyDescriptor... descriptors) {
			return new Mix(descriptors);
		}
	}

	static class Merge implements Properties	{
		private final Properties p1;
		private final Properties p2;

		public Merge(Properties p1, Properties p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		@Override
		public Collection<PropertyDescriptor> getDescriptors() {
			ArrayList<PropertyDescriptor> result = new ArrayList<PropertyDescriptor>();
			result.addAll(p1.getDescriptors());
			result.addAll(p2.getDescriptors());
			return result;
		}

		public static Properties add(Properties properties, PropertyDescriptor... toAdd) {
			if (toAdd == null || toAdd.length == 0)
				return properties;
			final List<PropertyDescriptor> list = Arrays.asList(toAdd);

			return merge(properties, new Properties() {
				@Override
				public Collection<PropertyDescriptor> getDescriptors() {
					return list;
				}
			});
		}

		public static Properties merge(Properties p1,	Properties p2) {
			if (p1 == null)
				return p2;
			if (p2 == null)
				return p1;
			return new Merge(p1, p2);
		}
	}
}
