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

package org.workcraft.plugins.interop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.Format;

public class DotGImporter implements Importer {
	private static String signalPattern = "([a-zA-Z\\_][a-zA-Z\\_0-9]*)(\\+|\\*|\\-|~|\\^[01])?(\\/([0-9]+))?";

	// create the lists of all of the transition types
	private Map<String, Type> types;
	private Map<String, MathNode> bem;




	public boolean accept(File file) {
		if (file.isDirectory())
			return true;
		if (file.getName().endsWith(".g"))
			return true;
		return false;
	}

	public String getDescription() {
		return "Signal Transition Graph (.g)";
	}


	public MathNode createComponent(STG stg, String nameid, Map<String, MathNode> bem) {
		// get the name of the first component
		Pattern p = Pattern.compile(signalPattern);
		Matcher m1 = p.matcher(nameid);

		// check whether this element is created already
		MathNode be1 = bem.get(nameid);


		if (be1==null) {
			// if not created, try to decide, how to create it
			if (m1.find()) {
				String name = m1.group(1);
				SignalTransition.Direction direction = Direction.TOGGLE;
				String sdir = m1.group(2);
				if (sdir!=null) {
					if (sdir.equals("+")) direction = Direction.PLUS;
					if (sdir.equals("-")) direction = Direction.MINUS;
				}

				String ins = m1.group(4);
				int instance = 0;
				if (ins!=null&&!ins.isEmpty()) instance = Integer.valueOf(ins);

				Type type = types.get(name);

				if (type != null) {
					be1 = stg.createSignalTransition();
					final SignalTransition transition = (SignalTransition)be1;
					transition.setSignalName(name);
					transition.setSignalType(type);
					transition.setDirection(direction);
					//stg.setInstance(transition, instance);
				} else {
					// consider it as the place
					be1 = stg.createPlace();
					// place label is used inside the program, but
					// can be deleted after the import
					//FIXME!!!!! be1.setIdentifier(nameid);
					type = null;
				}

				bem.put(nameid, be1);
			}
		}

		return be1;
	}

	public Model importFrom (InputStream in) throws DeserialisationException {
		types = new HashMap<String, Type>();
		implicitArcs = new HashMap<ImplicitArc, Place>();
		bem = new TreeMap<String, MathNode>();

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(in));

			//		BufferedReader br = new BufferedReader(new FileReader(file));
			String str;
			String s[];

			// read heading
			while ((str=br.readLine())!=null) {
				s = splitToTokens(str);
				if (s.length==0) continue;
				if (s[0].charAt(0)=='#') continue;

				if (s[0].equals(".inputs"))
					assignSignalsType(s, Type.INPUT);
				else if (s[0].equals(".outputs"))
					assignSignalsType(s, Type.OUTPUT);
				else if (s[0].equals(".internal"))
					assignSignalsType(s, Type.INTERNAL);
				else if (s[0].equals(".dummy"))
					throw new org.workcraft.exceptions.NotImplementedException();
					//assignSignalsType(s, Type.DUMMY);

				if (s[0].equals(".graph")) break;
			}

			// if neither type of transition was found, just quit
			if (types.isEmpty()) return new STG();

			MathNode be1, be2; // first and second connection candidates
			Pattern p;
			Matcher m;

			STG stg = new STG();

			// read connections
			while ((str=br.readLine())!=null) {
				s = splitToTokens(str);
				if (s.length==0) continue;
				if (s[0].charAt(0)=='#') continue;

				if (s[0].equals(".capacity")) {
					p = Pattern.compile(".capacity ([^#]*)");
					m = p.matcher(str);
					if (!m.find()) continue;
					str = m.group(1).trim();
					s = splitToTokens(str);

					for (int i=0;i<s.length;i++) {

						if (s[i].charAt(0)!='<') {
							// simple case, just find the place and set the capacity
							p = Pattern.compile("([a-zA-Z\\_][a-zA-Z\\_0-9\\/]*)(=([0-9]+))?");
							m = p.matcher(s[i]);
							if (m.find()) {
								str=m.group(1); // name of the signal

								if (m.group(m.groupCount())!=null) {
									((Place)bem.get(str)).setCapacity(Integer.valueOf(m.group(m.groupCount())));
								}
							}

						} else {

							str = "\\<("+signalPattern+"),("+signalPattern+")\\>(=([0-9]+))?";
							p = Pattern.compile(str);
							m = p.matcher(s[i]);

							if (m.find()) {
								// groups 1 and 6 correspond to full transition names
								SignalTransition et1 = (SignalTransition)bem.get(m.group(1));
								SignalTransition et2 = (SignalTransition)bem.get(m.group(6));

								if (et1!=null&&et2!=null) {

									Place place = getImplicitPlace(et1, et2);

									if (m.group(m.groupCount())!=null) {
										place.setCapacity(Integer.valueOf(m.group(m.groupCount())));
									}
								}

							}
						}

					}
					continue;
				}

				if (s[0].equals(".marking")) {
					p = Pattern.compile(".marking \\{([^#]*)\\}");
					m = p.matcher(str);
					if (!m.find()) continue;
					str = m.group(1).trim();

					s = splitToTokens(str);

					// read starting markings
					for (int i=0;i<s.length;i++) {

						if (s[i].charAt(0)!='<') {
							// simple case, just find the place and put the tokens
							p = Pattern.compile("([a-zA-Z\\_][a-zA-Z\\_0-9\\/]*)(=([0-9]+))?");
							m = p.matcher(s[i]);
							if (m.find()) {
								str=m.group(1); // name of the signal

								if (m.group(m.groupCount())!=null) {
									((Place)bem.get(str)).setTokens(Integer.valueOf(m.group(m.groupCount())));
								} else {
									((Place)bem.get(str)).setTokens(1);
								}
							}

						} else {

							str = "\\<("+signalPattern+"),("+signalPattern+")\\>(=([0-9]+))?";
							p = Pattern.compile(str);
							m = p.matcher(s[i]);

							if (m.find()) {
								//									for (int j=0;j<=m.groupCount();j++)
								//										System.out.println(m.group(j));
								// groups 1 and 6 correspond to full transition names
								SignalTransition et1 = (SignalTransition)bem.get(m.group(1));
								SignalTransition et2 = (SignalTransition)bem.get(m.group(6));

								if (et1!=null&&et2!=null) {
									Place implicitPlace = getImplicitPlace(et1, et2);

									if (m.group(m.groupCount())!=null) {
										(implicitPlace).setTokens(Integer.valueOf(m.group(m.groupCount())));
									} else {
										(implicitPlace).setTokens(1);
									}
								}

								// TODO: finish this part...
							}
						}

					}
					continue;
				}

				if (s[0].charAt(0)=='.') continue; // ignore other lines beginning with '.' (some unimplemented features?)

				be1 = createComponent(stg, s[0],  bem);

				for (int i=1;i<s.length;i++) {

					if (s[i].charAt(0)=='#') break;

					be2 = createComponent(stg, s[i], bem);

					if (be1 instanceof SignalTransition && be2 instanceof SignalTransition)
					{
						connectTransitions(stg, (SignalTransition)be1, (SignalTransition)be2);
					}
					else
					{
						try {
							stg.connect(be1, be2);
						} catch (InvalidConnectionException e) {
							e.printStackTrace();
						}
					}
				}

			}
			return stg;

		} catch (NumberFormatException e) {
			throw new DeserialisationException(e);
		} catch (FileNotFoundException e) {
			throw new DeserialisationException(e);
		} catch (IOException e) {
			throw new DeserialisationException(e);
		}
	}

	private void assignSignalsType(String[] s, Type input) {
		for (int i=1;i<s.length;i++) {

			if (s[i].charAt(0)=='#') break;

			types.put(s[i], input);
		}
	}

	private void connectTransitions(STG stg, SignalTransition be1, SignalTransition be2) {
		Place implicitPlace = stg.createPlace();

		implicitArcs.put(new ImplicitArc(be1, be2), implicitPlace);

		try {
			stg.connect(be1, implicitPlace);
			stg.connect(implicitPlace, be2);
		} catch (InvalidConnectionException e) {
			e.printStackTrace();
		}
	}

	class ImplicitArc
	{
		private SignalTransition first;
		private SignalTransition second;
		public ImplicitArc(SignalTransition first, SignalTransition second)
		{
			if(first == null || second == null)
				throw new NullPointerException();
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof ImplicitArc))
				return false;
			ImplicitArc other = (ImplicitArc)obj;
			return first.equals(other.first) && second.equals(other.second);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(new Object[]{first, second});
		}
	}

	Map<ImplicitArc, Place> implicitArcs = new HashMap<ImplicitArc, Place> ();

	private Place getImplicitPlace(SignalTransition et1, SignalTransition et2) {
		return implicitArcs.get(new ImplicitArc(et1, et2));
	}

	private static String[] splitToTokens(String str) {
		String[] split = str.split("[ \\t\\v\\f]+");

		ArrayList<String> result = new ArrayList<String>();

		for(String s : split)
			if(s.length() != 0)
				result.add(s);

		return result.toArray(new String [result.size()]);
	}

	public UUID getFormatUUID() {
		return Format.STG;
	}
}