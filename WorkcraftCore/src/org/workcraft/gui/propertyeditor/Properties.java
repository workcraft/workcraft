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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.util.Pair;

public interface Properties {
	public Collection<PropertyDescriptor> getDescriptors();

	public class Mix implements Properties {
		private LinkedList<PropertyDescriptor> mixDescriptors = new LinkedList<PropertyDescriptor>();

		public Mix(PropertyDescriptor... descriptors) {
			for (PropertyDescriptor descriptor : descriptors)
				this.mixDescriptors.add(descriptor);
		}

		@Override
		public Collection<PropertyDescriptor> getDescriptors() {
			return mixDescriptors;
		}

		public void add (PropertyDescriptor descriptor) {
			if (descriptor != null)
				mixDescriptors.add(descriptor);
		}

		public void add (Properties properties) {
			if (properties != null)
				for (PropertyDescriptor desc : properties.getDescriptors())
					mixDescriptors.add(desc);
		}

		public boolean isEmpty() {
			return mixDescriptors.isEmpty();
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


	public class Combine implements Properties {
		private LinkedList<PropertyDescriptor> combinedDescriptors = new LinkedList<PropertyDescriptor>();

		public Combine(PropertyDescriptor... descriptors) {
			LinkedHashMap<Pair<String, Class<?>>, Set<PropertyDescriptor>> categories =
					new LinkedHashMap<Pair<String, Class<?>>, Set<PropertyDescriptor>>();

			for (PropertyDescriptor descriptor: descriptors) {
				if (descriptor.isCombinable()) {
					Pair<String, Class<?>> key = new Pair<String, Class<?>>(descriptor.getName(), descriptor.getType());
					Set<PropertyDescriptor> value = categories.get(key);
					if (value == null) {
						value = new HashSet<PropertyDescriptor>();
						categories.put(key, value);
					}
					value.add(descriptor);
				}
			}

			for (Pair<String, Class<?>> key: categories.keySet()) {
				final String name = key.getFirst();
				final Class<?> type = key.getSecond();
				final Set<PropertyDescriptor> values = categories.get(key);
				PropertyDescriptor comboDescriptor = new PropertyDescriptor() {
					@Override
					public Object getValue() throws InvocationTargetException {
						Object result = null;
						for (PropertyDescriptor descriptor: values) {
							if (result == null) {
								result = descriptor.getValue();
							} else if (!result.equals(descriptor.getValue())) {
								return null;
							}
						}
						return result;
					}

					@Override
					public void setValue(Object value)  throws InvocationTargetException {
						for (PropertyDescriptor descriptor: values) {
							descriptor.setValue(value);
						}
					}

					@Override
					public Map<? extends Object, String> getChoice() {
						Map<? extends Object, String> result = null;
						for (PropertyDescriptor descriptor: values) {
							result = descriptor.getChoice();
						}
						return result;
					}

					@Override
					public String getName() {
						return name;
					}

					@Override
					public Class<?> getType() {
						return type;
					}

					@Override
					public boolean isWritable() {
						boolean result = true;
						for (PropertyDescriptor descriptor: values) {
							result = result && descriptor.isWritable();
						}
						return result;
					}

					@Override
					public boolean isCombinable() {
						boolean result = true;
						for (PropertyDescriptor descriptor: values) {
							result = result && descriptor.isCombinable();
						}
						return result;
					}
				};
				combinedDescriptors.add(comboDescriptor);
			}
		}

		@Override
		public Collection<PropertyDescriptor> getDescriptors() {
			return combinedDescriptors;
		}

		public boolean isEmpty() {
			return combinedDescriptors.isEmpty();
		}

		public static Combine from (PropertyDescriptor... descriptors) {
			return new Combine(descriptors);
		}

		public static Combine from (Collection<PropertyDescriptor> descriptors) {
			return new Combine(descriptors.toArray(new PropertyDescriptor[0]));
		}
	}

}
