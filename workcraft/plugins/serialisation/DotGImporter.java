package org.workcraft.plugins.serialisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.serialisation.Format;
import org.workcraft.framework.serialisation.Importer;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class DotGImporter implements Importer {
	private static String signalPattern = "([a-zA-Z\\_][a-zA-Z\\_0-9]*)(\\+|\\*|\\-|~|\\^[01])?(\\/([0-9]+))?";

	public boolean accept(File file) {
		if (file.isDirectory())
			return true;
		if (file.getName().endsWith(".g"))
			return true;
		return false;
	}

	public String getDescription() {
        return ".g files (Petrify, PUNF)";
    }


	// create the lists of all of the transition types
	HashSet<String> internal	= new HashSet<String>();
	HashSet<String> inputs		= new HashSet<String>();
	HashSet<String> outputs		= new HashSet<String>();
	HashSet<String> dummy		= new HashSet<String>();

	public MathNode createComponent(String nameid, STG doc, SortedMap<String, MathNode> bem) {


		// get the name of the first component
		Pattern p = Pattern.compile(signalPattern);
//		Pattern p2 = Pattern.compile("^p([0-9]+)$");
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

				if (inputs.contains(name)) {
					be1 = doc.createSignalTransition();

					((SignalTransition)be1).setSignalName(name);
					((SignalTransition)be1).setType(Type.INPUT);
					((SignalTransition)be1).setDirection(direction);
					if (instance!=0) ((SignalTransition)be1).setInstance(instance);

				} else if (outputs.contains(name)) {
					be1 = doc.createSignalTransition();

					((SignalTransition)be1).setSignalName(name);
					((SignalTransition)be1).setType(Type.OUTPUT);
					((SignalTransition)be1).setDirection(direction);
					if (instance!=0) ((SignalTransition)be1).setInstance(instance);

				} else if (internal.contains(name)) {
					be1 = doc.createSignalTransition();

					((SignalTransition)be1).setSignalName(name);
					((SignalTransition)be1).setType(Type.INTERNAL);
					((SignalTransition)be1).setDirection(direction);
					if (instance!=0) ((SignalTransition)be1).setInstance(instance);

				} else if (dummy.contains(name)) {
					be1 = doc.createSignalTransition();

					((SignalTransition)be1).setSignalName(name);
					((SignalTransition)be1).setType(Type.DUMMY);
					((SignalTransition)be1).setDirection(direction);
					if (instance!=0) ((SignalTransition)be1).setInstance(instance);
				} else {
					// consider it as the place
					be1 = doc.createPlace();
					// place label is used inside the program, but
					// can be deleted after the import
					be1.setLabel(nameid);
				}
				bem.put(nameid, be1);

			}
		}

		return be1;
	}

	public Model importFrom (ReadableByteChannel in) {
		STG stg = new STG();

//		File file = new File(path);

//		Scanner scanner = new Scanner(file);

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(Channels.newInputStream(in)));

			//		BufferedReader br = new BufferedReader(new FileReader(file));
					String str;
					String s[];
					String inputList="";
					String outputList="";

					// read heading
					while ((str=br.readLine())!=null) {
						s = splitToTokens(str);
						if (s.length==0) continue;
						if (s[0].charAt(0)=='#') continue;

						if (s[0].equals(".inputs"))
							for (int i=1;i<s.length;i++) {
								if (s[i].charAt(0)=='#') break;
								inputs.add(s[i]);
								if (!s[i].equals("")) inputList+=" ";
								inputList+=s[i];
							}

						if (s[0].equals(".outputs"))
							for (int i=1;i<s.length;i++) {
								if (s[i].charAt(0)=='#') break;
								outputs.add(s[i]);
								if (!s[i].equals("")) outputList+=" ";
								outputList+=s[i];
							}

						if (s[0].equals(".internal"))
							for (int i=1;i<s.length;i++) {
								if (s[i].charAt(0)=='#') break;
								internal.add(s[i]);
							}

						if (s[0].equals(".dummy"))
							for (int i=1;i<s.length;i++) {
								if (s[i].charAt(0)=='#') break;
								dummy.add(s[i]);
							}

						if (s[0].equals(".graph")) break;
					}

					// if neither type of transition was found, just quit
					if (inputs.isEmpty()&&outputs.isEmpty()&&internal.isEmpty()&&dummy.isEmpty()) return stg;


					MathNode be1, be2; // first and second connection candidates
					Pattern p;
					Matcher m;
					SortedMap<String, MathNode> bem = new TreeMap<String, MathNode>();

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
//									for (int j=0;j<=m.groupCount();j++)
//										System.out.println(m.group(j));
										// groups 1 and 6 correspond to full transition names
										SignalTransition et1 = (SignalTransition)bem.get(m.group(1));
										SignalTransition et2 = (SignalTransition)bem.get(m.group(6));

										if (et1!=null&&et2!=null) {
											Collection<Node> ep = stg.getPostset(et1);
											ep.retainAll(stg.getPreset(et2));

											Iterator<Node> it = ep.iterator();

											if (m.group(m.groupCount())!=null) {
												((Place)it.next()).setCapacity(Integer.valueOf(m.group(m.groupCount())));
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
											Collection<Node> ep = stg.getPostset(et1);
											ep.retainAll(stg.getPreset(et2));

											Iterator<Node> it = ep.iterator();

											if (m.group(m.groupCount())!=null) {
												((Place)it.next()).setTokens(Integer.valueOf(m.group(m.groupCount())));
											} else {
												((Place)it.next()).setTokens(1);
											}
										}

										// TODO: finish this part...
									}
								}

							}
							continue;
						}

						if (s[0].charAt(0)=='.') continue; // ignore other lines beginning with '.' (some unimplemented features?)


						be1 = createComponent(s[0], stg, bem);

//					System.out.print(s[0] + " connects to:");
						for (int i=1;i<s.length;i++) {

							if (s[i].charAt(0)=='#') break;

							be2 = createComponent(s[i], stg, bem);

							if (be1 instanceof SignalTransition && be2 instanceof SignalTransition)
							{
								Place implicitPlace = stg.createPlace();
								try {
									stg.connect(be1, implicitPlace);
									stg.connect(implicitPlace, be2);
								} catch (InvalidConnectionException e) {
									e.printStackTrace();
								}
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

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stg;
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