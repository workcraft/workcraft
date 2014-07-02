package org.workcraft.plugins.cpog;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.workcraft.dom.visual.RemovedNodeDeselector;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.cpog.EncoderSettings.generationMode;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.CpogEncoding;
import org.workcraft.plugins.cpog.optimisation.CpogOptimisationTask;
import org.workcraft.plugins.cpog.optimisation.DefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.OneHotNumberProvider;
import org.workcraft.plugins.cpog.optimisation.Optimiser;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.util.Func;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

import com.sun.org.apache.regexp.internal.RE;

public class CpogProgrammer {

	private EncoderSettings settings;
	private File scenarioFile, encodingFile ;
	private String toolPath = "../tools/";
	private int bits = 1;
	private Double minArea;

	// SETTING PARAMETERS FOR CALLING PROGRAMMER.X
	private String espressoCommand, abcFolder , gatesLibrary ,
			verbose = "", genMode= "", numSol= "", customFlag= "", customPath= "", effort= "", espressoFlag= "", abcFlag= "", gateLibFlag= "", cpogSize= "", disableFunction= "",
			oldSynt= "";
	// Allocation data structures
	private Process process;
	private String[] opt_enc, opt_formulaeVertices,truthTableVertices, opt_vertices, opt_sources, opt_dests,
			opt_formulaeArcs, truthTableArcs, arcNames;
	private int v,a,n;

	public CpogProgrammer(EncoderSettings settings){
		this.setSettings(settings);
	}

	private String binaryToInt(String string) {
		int value = 0, wg = 1;
		if(string != null){
			for(int i = string.length()-1; i>=0; i--){
				if(string.charAt(i) == '1'){
					value += wg;
				}
				wg *= 2;
			}

			return String.valueOf(value);
		}
		return "0";
	}

	private static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i = 0; i < children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    return dir.delete(); // The directory is empty now and can be deleted.
	}

	private String generateConstraint(char [][][] constraints, int numScenarios, int event1, int event2)
	{
		StringBuilder s = new StringBuilder();
		for(int k = 0; k < numScenarios; k++) s.append(constraints[k][event1][event2]);
		return s.toString();
	}

	private char trivialEncoding(char [][][] constraints, int numScenarios, int event1, int event2)
	{
		char trivial = '-';

		for(int k = 0; k < numScenarios; k++)
		{
			if (constraints[k][event1][event2] == '0')
			{
				if (trivial == '1') return '?';
				trivial = '0';
			}

			if (constraints[k][event1][event2] == '1')
			{
				if (trivial == '0') return '?';
				trivial = '1';
			}
		}

		return trivial;
	}

	private int WriteCpogIntoFile(int m, ArrayList<VisualScenario> scenarios)
	{
		try{
			scenarioFile = File.createTempFile("scenarios", "cpog");

		     PrintStream Output = new PrintStream(scenarioFile);


			for(int k = 0; k < m; k++)
			{
				Map nodes = new HashMap<>();
				// Print arcs
				Output.println(".scenario CPOG_" + k);
				for(VisualConnection c : scenarios.get(k).getConnections()){
					if (c instanceof VisualArc)
					{
						VisualArc arc = (VisualArc)c;
						VisualComponent c1 = arc.getFirst(), c2 = arc.getSecond();
						if (c1 instanceof VisualVertex && c2 instanceof VisualVertex)
						{
							nodes.put(c1.getLabel(), 0);
							nodes.put(c2.getLabel(), 0);
							Output.println(c1.getLabel() + " " + c2.getLabel());
						}
					}
				}

				// Print conditions on vertices
				for(VisualComponent component : scenarios.get(k).getComponents()){
					if(component instanceof VisualVertex){
						VisualVertex vertex = (VisualVertex)component;
						BooleanFormula condition = vertex.getCondition();
						if (condition != One.instance() && condition != Zero.instance()){

							// Format output by substituting ' with !
							String cond = FormulaToString.toString(condition).replaceAll("'", "!");
							String result = "";
							String tmp = "";
							for(int i=0; i<cond.length(); i++){
								if(cond.charAt(i) != '(' && cond.charAt(i) != ')' && cond.charAt(i) != '+' &&
										cond.charAt(i) != '*' && cond.charAt(i) != '!' && cond.charAt(i) != ' '){
									tmp = "";
									while(i < cond.length() && cond.charAt(i) != '(' && cond.charAt(i) != ')' && cond.charAt(i) != '+' &&
											cond.charAt(i) != '*' && cond.charAt(i) != '!' && cond.charAt(i) != ' '){
										tmp += cond.charAt(i);
										i++;
									}
									//System.out.println("TMP: " + tmp);
									for(int j= tmp.length()-1; j >= 0; j--){
										//System.out.println(j + ") " +  tmp.charAt(j));
										result += tmp.charAt(j);
									}
									if(i < cond.length()){
										result += cond.charAt(i);
									}
								}
								else{
									result += cond.charAt(i);;
								}
							}

							String end = "";
							for(int i = 0; i<result.length(); i++){
								if(result.charAt(i) == '(') end += ')';
								else if (result.charAt(i) == ')') end += '(';
								else end += result.charAt(i);
							}

							// Print conditions on each vertices
							Output.print(":");
							for(int i=end.length()-1; i>=0; i--){
								Output.print(end.charAt(i));
							}
							Output.println(" " + vertex.getLabel());
						}

						//VisualVertex vertex = (VisualVertex)component;
						if(!nodes.containsKey(vertex.getLabel())){
							Output.println(vertex.getLabel());
						}
					}

				}
				Output.println(".end");
				if(k != m-1){
					Output.println();
				}
			}
			Output.close();

			// WRITING CUSTOM ENCODING FILE
			if(settings.getGenMode() != generationMode.SCENCO){
				encodingFile = File.createTempFile("custom", "enc");
				if(settings.isCustomEncMode()){
					    PrintStream Output1 = new PrintStream(encodingFile);

						String [] enc = settings.getCustomEnc();
						for(int k = 0; k < m; k++)
						{
							if(enc[k].contains("2") || enc[k].contains("3") || enc[k].contains("4") ||
									enc[k].contains("5") || enc[k].contains("6") || enc[k].contains("7") ||
									enc[k].contains("8") || enc[k].contains("9")){
								JOptionPane.showMessageDialog(null,
										"Op-code " + enc[k] + " not allowed.",
										"Custom encoding error",
										JOptionPane.ERROR_MESSAGE);
								return -1;

							}
							String empty = "";
							for(int i=0; i<settings.getBits(); i++) empty += 'X';
							if(enc[k].equals("") || enc[k].equals(empty)){
								Output1.println("/");
							}
							else{
								Output1.println(enc[k]);
							}
						}
						Output1.println(settings.getBits());
						Output1.close();
				}
			}
		}catch (IOException e) {
			System.out.println("Error: " + e);
		}

		return 0;
	}

	private void printController(int m){
		System.out.println();
		String fileName = toolPath + "results/generated_encoding/";
		for(int i=0; i<m; i++) fileName = fileName.concat(binaryToInt(opt_enc[i]) + "_");
		fileName = fileName.concat(".prg");
		File f = new File(fileName);
		if(f.exists() && !f.isDirectory()){
			System.out.println("Boolean controller:");
			try{
				  FileInputStream fstream = new FileInputStream(fileName);
				  DataInputStream in = new DataInputStream(fstream);
				  BufferedReader bre = new BufferedReader(new InputStreamReader(in));
				  String strLine;
				  bre.readLine();
				  bre.readLine();
				  while ((strLine = bre.readLine()) != null)   {
					  System.out.println (strLine);
				  }
				  in.close();
			}catch (Exception e){ //Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
			System.out.println();
		}
		return;
	}

	private void deleteTempFiles(){
		if(scenarioFile.exists()) scenarioFile.delete();
		if(encodingFile.exists()) encodingFile.delete();
		return;
	}

	private int callingProgrammer(Double currArea, WorkspaceEntry we, int it, boolean continuous) throws IOException{
		//Debug Printing: launching executable
		/*System.out.println(toolPath + "programmer.x" + " " + scenarioFile.getAbsolutePath() + " " +
				"-m" + " " + effort + " " + genMode + " " + numSol + " " + customFlag + " " + customPath + " " +
				verbose + " " + cpogSize + " " + disableFunction + " " + oldSynt + " " +
				espressoFlag + " " + espressoCommand + " " + abcFlag + " " + abcFolder + " " + gateLibFlag + " " +
				gatesLibrary);*/
		process = new ProcessBuilder(toolPath + "programmer.x", scenarioFile.getAbsolutePath(),
				"-m",effort,genMode, numSol,customFlag,customPath,verbose,cpogSize,disableFunction,oldSynt,
				espressoFlag,espressoCommand, abcFlag, abcFolder, gateLibFlag, gatesLibrary).start();
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		boolean finish = false;
		if(continuous){
			while ( (line = br.readLine()) != null && finish == false) {
				// Read Area
				if(line.contains(".area")){
					line = br.readLine();
					currArea = Double.valueOf(line);
					line = br.readLine();
					if(currArea < minArea){
						v = 0; a = 0;
						minArea = currArea;
						System.out.println(it + ") " + "Area of current circuit: " + minArea);
						while((line = br.readLine()) != null){
							// Read Optimal Encoding
							if(line.contains("MIN: ")){
								StringTokenizer st2 = new StringTokenizer(line, " ");
								int j = 0;
								st2.nextElement();
								while (st2.hasMoreElements()) {
									opt_enc[j++] = (String) st2.nextElement();
								}
							}

							// Read Optimal Formulae
							if(line.contains(".start_formulae")){
								line = br.readLine();
								while(line.contains(".end_formulae") == false){
									StringTokenizer st2 = new StringTokenizer(line, ",");
									String el = (String)st2.nextElement();
									if(el.equals("V")){ //formula of a vertex
										opt_vertices[v] = (String) st2.nextElement();
										truthTableVertices[v] = (String) st2.nextElement();
										opt_formulaeVertices[v++] = (String) st2.nextElement();
									}else{
										opt_sources[a] = (String) st2.nextElement();
										opt_dests[a] = (String) st2.nextElement();
										arcNames[a] = opt_sources[a] + "->" + opt_dests[a];
										truthTableArcs[a] = (String) st2.nextElement();
										opt_formulaeArcs[a++] = (String) st2.nextElement();
									}
									line = br.readLine();
								}

							}

							// Read statistics
							if(line.contains(".statistics")){
								line = br.readLine();
								while(line.contains(".end_statistics") == false){
									line = br.readLine();
								}
							}

							// Read errors
							if(line.contains(".error")){
								line = br.readLine();
								while(line.contains(".end_error") == false){
									JOptionPane.showMessageDialog(null,
											line,
											"Programmer.x error",
											JOptionPane.ERROR_MESSAGE);
									line = br.readLine();
								}
								return -1;

							}
						}
					}else{
						finish = true;
					}
				}

			}
		}else{
			while ( (line = br.readLine()) != null){
				if(settings.isVerboseMode())
					System.out.println(line);

				// Read Optimal Encoding
				if(line.contains("MIN: ")){
					StringTokenizer st2 = new StringTokenizer(line, " ");
					int j = 0;
					st2.nextElement();
					while (st2.hasMoreElements()) {
						opt_enc[j++] = (String) st2.nextElement();
					}
				}

				// Read Optimal Formulae
				if(line.contains(".start_formulae")){
					line = br.readLine();
					while(line.contains(".end_formulae") == false){
						if(settings.isVerboseMode())
							System.out.println(line);
						StringTokenizer st2 = new StringTokenizer(line, ",");
						String el = (String)st2.nextElement();
						if(el.equals("V")){ //formula of a vertex
							opt_vertices[v] = (String) st2.nextElement();
							truthTableVertices[v] = (String) st2.nextElement();
							opt_formulaeVertices[v++] = (String) st2.nextElement();
						}else{
							opt_sources[a] = (String) st2.nextElement();
							opt_dests[a] = (String) st2.nextElement();
							arcNames[a] = opt_sources[a] + "->" + opt_dests[a];
							truthTableArcs[a] = (String) st2.nextElement();
							opt_formulaeArcs[a++] = (String) st2.nextElement();
						}
						line = br.readLine();
					}

				}

				// Read statistics
				if(line.contains(".statistics")){
					line = br.readLine();
					while(line.contains(".end_statistics") == false){
						System.out.println(line);
						line = br.readLine();
					}
				}

				// Read errors
				if(line.contains(".error")){
					line = br.readLine();
					while(line.contains(".end_error") == false){
						JOptionPane.showMessageDialog(null,
								line,
								"Programmer.x error",
								JOptionPane.ERROR_MESSAGE);
						line = br.readLine();
					}
					return -1;

				}
			}
		}

		process.destroy();
		is.close();
		isr.close();
		br.close();
		return 0;
	}

	private void reset_vars(){
		verbose = ""; genMode= ""; numSol= ""; customFlag= ""; customPath= ""; effort= ""; espressoFlag= "";
		abcFlag= ""; gateLibFlag= ""; cpogSize= ""; disableFunction= ""; oldSynt= "";

		return;
	}

	public void run(WorkspaceEntry we)
	{
		VisualCPOG cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());

		we.captureMemento();

		reset_vars();

		HashMap<String, Integer> events = new HashMap<String, Integer>();
		n = 0;
		ArrayList<Point2D> positions = new ArrayList<Point2D>();
		ArrayList<Integer> count = new ArrayList<Integer>();

		// TODO: remove deprecated method
		ArrayList<VisualScenario> scenarios = new ArrayList<VisualScenario>(cpog.getGroups());

		// Scenario contains single graphs compose CPOG
		int m = scenarios.size();

		// If less than two, do not encode scenarios
		if (m < 2)
		{
			JOptionPane.showMessageDialog(null,
					"At least two scenarios are expected.",
					"Not enough scenarios",
					JOptionPane.ERROR_MESSAGE);
			we.cancelMemento();
			deleteTempFiles();
			return;
		}

		// Scan every scenarios
		for(int k = 0; k < m; k++)
		{
			// Scan every elements of each scenario
			for(VisualComponent component : scenarios.get(k).getComponents())
			if (component instanceof VisualVertex)	// If element is a vertex
			{
				VisualVertex vertex = (VisualVertex)component;

				if (!events.containsKey(vertex.getLabel())) // Check if a condition is present on vertex
				{
					events.put(vertex.getLabel(), n);
					count.add(1);
					Point2D p = vertex.getCenter();
					p.setLocation(p.getX() - scenarios.get(k).getBoundingBox().getMinX(), p.getY() - scenarios.get(k).getBoundingBox().getMinY());
					positions.add(p);
					n++;
				}
				else
				{
					int id = events.get(vertex.getLabel());
					count.set(id, count.get(id) + 1);
					Point2D p = vertex.getCenter();
					p.setLocation(p.getX() - scenarios.get(k).getBoundingBox().getMinX(), p.getY() - scenarios.get(k).getBoundingBox().getMinY());
					positions.set(id, Geometry.add(positions.get(id), p));
				}
			}
		}

		// construct constraints

		char [][][] constraints = new char[m][n][n];
		int [][] graph = new int[n][n];

		for(int k = 0; k < m; k++)
		{
			for(int i = 0; i < n; i++) for(int j = 0; j < n; j++) {
				constraints[k][i][j] = '0';
			}

			for(VisualComponent component : scenarios.get(k).getComponents())
			if (component instanceof VisualVertex)
			{
				VisualVertex vertex = (VisualVertex)component;
				int id = events.get(vertex.getLabel());
				constraints[k][id][id] = '1';
			}

			for(int i = 0; i < n; i++) for(int j = 0; j < n; j++) graph[i][j] = 0;

			for(VisualConnection c : scenarios.get(k).getConnections())
			if (c instanceof VisualArc)
			{
				VisualArc arc = (VisualArc)c;
				VisualComponent c1 = arc.getFirst(), c2 = arc.getSecond();
				if (c1 instanceof VisualVertex && c2 instanceof VisualVertex)
				{
					int id1 = events.get(((VisualVertex)c1).getLabel());
					int id2 = events.get(((VisualVertex)c2).getLabel());
					graph[id1][id2] = 1;
				}
			}

			// compute transitive closure

			for(int t = 0; t < n; t++)
				for(int i = 0; i < n; i++)
					if (graph[i][t] > 0)
						for(int j = 0; j < n; j++)
							if (graph[t][j] > 0) graph[i][j] = 1;

			// detect transitive arcs

			for(int t = 0; t < n; t++)
				for(int i = 0; i < n; i++)
					if (graph[i][t] > 0)
						for(int j = 0; j < n; j++)
							if (graph[t][j] > 0) graph[i][j] = 2;

			// report cyclic scenario

			for(int i = 0; i < n; i++)
				if (graph[i][i] > 0)
				{
					deleteTempFiles();
					JOptionPane.showMessageDialog(null,
												"Scenario '" + scenarios.get(k).getLabel() + "' is cyclic.",
												"Invalid scenario",
												JOptionPane.ERROR_MESSAGE);
					we.cancelMemento();
					return;
				}

			for(int i = 0; i < n; i++)
				for(int j = 0; j < n; j++)
				if (i != j)
				{
					char ch = '0';

					if (graph[i][j] > 0) ch = '1';
					if (graph[i][j] > 1) ch = '-';
					if ( constraints[k][i][i] == '0' || constraints[k][j][j] == '0' ) ch = '-';

					constraints[k][i][j] = ch;
				}
		}

		// Write scenarios into file.
		int res;
		if((res = WriteCpogIntoFile(m, scenarios)) != 0){
			deleteTempFiles();
			if(res != -1){
				JOptionPane.showMessageDialog(null,
						"Error on writing scenario file.",
						"Workcraft error",
						JOptionPane.ERROR_MESSAGE);
			}
			we.cancelMemento();
			return;
		}

		espressoCommand = CpogSettings.getEspressoCommand();
		abcFolder = CpogSettings.getAbcFolder();
		gatesLibrary = CpogSettings.getGatesLibrary();
		opt_enc = new String[m];
		opt_formulaeVertices = new String[n*n];
		truthTableVertices =  new String[n*n];
		opt_vertices = new String[n];
		opt_sources = new String[n*n];
		opt_dests = new String[n*n];
		opt_formulaeArcs = new String[n*n];
		truthTableArcs =  new String[n*n];
		arcNames = new String[n*n];
		espressoCommand = CpogSettings.getEspressoCommand();
		abcFolder = CpogSettings.getAbcFolder();
		gatesLibrary = CpogSettings.getGatesLibrary();
		espressoFlag = "-e";
		v=0;
		a=0;

		// CALLING PROGRAMMER.X
		boolean SCENCO = false;
		try {
			File f;
			f = new File(espressoCommand);
			if(!f.exists() || f.isDirectory()){
				deleteTempFiles();
				JOptionPane.showMessageDialog(null,
						"Espresso tool is needed to programmer to work properly",
						"Espresso tool not present",
						JOptionPane.ERROR_MESSAGE);
				we.cancelMemento();
				return;
			}

			espressoCommand = espressoCommand.replace(" ", "\\ ");

			f = new File(abcFolder);
			if(!f.exists() || !f.isDirectory()){
				JOptionPane.showMessageDialog(null,
						"You can download it at http://www.eecs.berkeley.edu/~alanmi/abc/",
						"Abc tool not present",
						JOptionPane.ERROR_MESSAGE);
			}
			else{
				abcFlag = "-a";
				gateLibFlag = "-lib";
				f = new File(abcFolder + gatesLibrary);
				if(!f.exists() || f.isDirectory()){
					deleteTempFiles();
					JOptionPane.showMessageDialog(null,
							"It is needed to compute area of circuit properly",
							"Gate library not present",
							JOptionPane.ERROR_MESSAGE);
					we.cancelMemento();
					return;
				}
			}

			if(settings.isCpogSize()) cpogSize = "-cs";
			if(settings.isCostFunc()) disableFunction = "-d";
			if(settings.isVerboseMode()) verbose = "-v";
			if(settings.isEffort()) effort = "all";
			else effort = "min";
			if(settings.isCustomEncMode()){
				customFlag = "-set";
				customPath = encodingFile.getAbsolutePath();
			}
			switch(settings.getGenMode()){
				case OPTIMAL_ENCODING:
					genMode = "-top";
					numSol = String.valueOf(settings.getSolutionNumber());
					break;
				case RECURSIVE:
					if(settings.isCustomEncMode()){
						deleteTempFiles();
						JOptionPane.showMessageDialog(null,
								"Recursive encodings generation combined with custom op-codes is not supported.",
								"Encodings generation error",
								JOptionPane.ERROR_MESSAGE);
						we.cancelMemento();
						return;
					}
					break;
				case RANDOM:
					genMode = "-r";
					if(settings.isCustomEncMode()){
						deleteTempFiles();
						JOptionPane.showMessageDialog(null,
								"Random encodings generation combined with custom op-codes is not supported.",
								"Encodings generation error",
								JOptionPane.ERROR_MESSAGE);
						we.cancelMemento();
						return;
					}
					numSol = String.valueOf(settings.getSolutionNumber());
					break;
				case SCENCO:
					SCENCO = true;
					customFlag = "-set";
					genMode = "-top";
					numSol = "1";
					break;
				case OLD_SYNT:
					customFlag = "-set";
					customPath = encodingFile.getAbsolutePath();
					oldSynt = "-old";
					genMode = "-top";
					numSol = "1";
					break;
				default:
					System.out.println("Error");
			}

			deleteDir(new File(toolPath + "results/"));
			File d = new File("../tools/results/generated_encoding/");
			d.mkdirs();

		if(!SCENCO){
			// CALLING PROGRAMMER.X
			boolean out = false;
			boolean continuous = false;
			int limit, it = 0;
				if(settings.isContMode()){
					limit = 100;
					numSol = "1";
					continuous = true;
				}else{
					limit = 1;
				}
				minArea = Double.MAX_VALUE;
				Double currArea = Double.MAX_VALUE;
				while(!out && it < limit){
					if(callingProgrammer(currArea, we,it, continuous) != 0){
						deleteTempFiles();
						we.cancelMemento();
						return;
					}
					it++;
				}
				// Print controller
				printController(m);

			}
			} catch (IOException e1) {
				System.out.println("Error.");
				e1.printStackTrace();
			}
		// group similar constraints
		HashMap<String, BooleanFormula> formulaeName = new HashMap<String, BooleanFormula>();
		HashMap<String, Integer> task = new HashMap<String, Integer>();
		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
				if (trivialEncoding(constraints, m, i, j) == '?')
				{
					String constraint = generateConstraint(constraints, m, i, j);
					if (!task.containsKey(constraint)){
						task.put(constraint, task.size());
					}
				}

		// call CPOG encoder

		char [][] matrix = new char[m][task.size()];

		String [] instance = new String[m];
		for(String s : task.keySet())
			for(int i = 0; i < m; i++) matrix[i][task.get(s)] = s.charAt(i);

		for(int i = 0; i < m; i++) instance[i] = new String(matrix[i]);

		int freeVariables;
		if(settings.getGenMode() != generationMode.SCENCO)
			freeVariables = opt_enc[0].length();
		else
			freeVariables = settings.getBits();
		int derivedVariables = CpogSettings.getCircuitSize();

		Optimiser<OneHotIntBooleanFormula> oneHot = new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider());

		DefaultCpogSolver<BooleanFormula> solverCnf = new DefaultCpogSolver<BooleanFormula>(oneHot, new CleverCnfGenerator());

		VisualVariable predicatives[] = new VisualVariable[n];
		int pr = 0;
		for(VisualVariable variable : Hierarchy.getChildrenOfType(cpog.getRoot(), VisualVariable.class)) {
			predicatives[pr++] = variable;
		}

		Variable [] vars = new Variable[freeVariables + pr];
		for(int i = 0; i < freeVariables; i++) vars[i] = cpog.createVisualVariable().getMathVariable();
		for(int i = 0; i< pr; i++) vars[freeVariables +i] = predicatives[i].getMathVariable();

		// DEBUG PRINTING: printing variables needed to encode graph.
		/*System.out.println("PRINTING VARIABLES:");
		for(int i = 0; i< freeVariables + pr; i++)
			System.out.println(vars[i].getLabel());*/

		CpogEncoding solution = null;
		try
		{
			// SCENCO EXECUTION TO FIND VARIABLES AND FUNCTIONS
			solution = solverCnf.solve(instance, vars, derivedVariables);
			CpogOptimisationTask opt_task = (CpogOptimisationTask) solverCnf.getTask(instance, vars, derivedVariables);
			if (solution == null)
			{
				if(SCENCO){
					we.cancelMemento();
					JOptionPane.showMessageDialog(null, "SCENCO is not able to solve the CPOG, try other options.",
							"Encoding result", JOptionPane.ERROR_MESSAGE);
					deleteTempFiles();
					return;
				}
				System.out.println("INFORMATION: Scenco cannot solve the CPOG.");
				System.out.println();
			}

			System.out.println("Op-code selected for graphs:");
			for(int i=0; i<m; i++){
				String name;
				if(scenarios.get(i).getLabel().equals("")){
					name = "CPOG " + i;
				}
				else{
					name = scenarios.get(i).getLabel();
				}
				System.out.println(name + ": " + opt_enc[i]);
			}
			solution = new CpogEncoding(null, null);

			if(!SCENCO){

				solution = new CpogEncoding(null, null);
				BooleanFormula[][] encodingVars = opt_task.getEncodingVars();
				BooleanFormula[] formule = new BooleanFormula[v + a];
				// Set optimal formulae to graphs
				final Variable [] variables = vars;
				for(int i=0; i<v; i++){
					if(opt_formulaeVertices[i].contains("x")){
						BooleanFormula formula_opt = null;
						formula_opt = BooleanParser.parse(opt_formulaeVertices[i], new Func<String, BooleanFormula>() {

							@Override
							public BooleanFormula eval(String arg) {
								arg = arg.substring("x_".length());
								int id = Integer.parseInt(arg);
								return variables[id];
							}
						});

						formulaeName.put(opt_vertices[i], formula_opt);

						// OLD formulae array
						/*if(task.containsKey(truthTableVertices[i])){
							formule[task.get(truthTableVertices[i])] = formula_opt;
						}*/
					}
				}
				for(int i=0; i<a; i++){
					if(opt_formulaeArcs[i].contains("x")){
						BooleanFormula formula_opt = null;
						formula_opt = BooleanParser.parse(opt_formulaeArcs[i], new Func<String, BooleanFormula>() {
							@Override
							public BooleanFormula eval(String arg) {
								arg = arg.substring("x_".length());
								int id = Integer.parseInt(arg);
								return variables[id];
							}
						});

						formulaeName.put(arcNames[i], formula_opt);

						/*if(task.containsKey(truthTableArcs[i])){
							formule[task.get(truthTableArcs[i])] = formula_opt;
						}*/
					}
				}
				solution.setFormule(formule);

				// Set optimal encoding to graphs
				boolean[][] opt_encoding = new boolean[m][];
				for(int i=0;i<m;i++)
				{
					opt_encoding[i] = new boolean[freeVariables + pr];
					for(int j=0;j<freeVariables;j++){
						if(opt_enc[i].charAt(j) == '0' || opt_enc[i].charAt(j) == '-') opt_encoding[i][j] = false;
						else	opt_encoding[i][j] = true;
					}
					for(int j=freeVariables;j<freeVariables + pr;j++){
						opt_encoding[i][j] = false;
					}

				}
				solution.setEncoding(opt_encoding);
			}
		}
		catch(Exception e)
		{
			we.cancelMemento();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Encoding result", JOptionPane.ERROR_MESSAGE);
		}

		if(solution == null){
			return;
		}

		// create result

		boolean[][] encoding = solution.getEncoding();

		if(settings.getGenMode() == generationMode.SCENCO){

			try{
				 encodingFile = File.createTempFile("encoding", "cpog");
			     PrintStream Output = new PrintStream(encodingFile);

			     for(int i=0; i<m; i++){
			    	 for(int j=0; j<settings.getBits(); j++){
			    		 if(encoding[i][j]){
			    			 Output.print("1");
			    		 }
			    		 else{
			    			 Output.print("0");
			    		 }
			    	 }
			    	 Output.println();
			     }
			     Output.close();

			     customPath = encodingFile.getAbsolutePath();
			     if(callingProgrammer(Double.MAX_VALUE, we, 0, false) != 0){
			    	 deleteTempFiles();
					we.cancelMemento();
					return;
			     }

			     // Print controller
			     printController(m);
			}catch (IOException e) {
				System.out.println("Error: " + e);
			}
		}

		for(int k = 0; k < m; k++)
		{
			for(int i = 0; i < freeVariables; i++){
				scenarios.get(k).getEncoding().setState(vars[i], VariableState.fromBoolean(encoding[k][i]));
			}
			for(int i = freeVariables; i < freeVariables + pr; i++){
				scenarios.get(k).getEncoding().setState(vars[i], VariableState.fromBoolean(encoding[k][i]));
			}
		}

		VisualScenario result = cpog.createVisualScenario();
		result.setLabel("Composition");
		VisualVertex [] vertices = new VisualVertex[n];
		for(String eventName : events.keySet())
		{
			int id = events.get(eventName);
			vertices[id] = cpog.createVisualVertex(result);
			vertices[id].setLabel(eventName);
			vertices[id].setPosition(Geometry.multiply(positions.get(id), 1.0/count.get(id)));
			if(formulaeName.containsKey(eventName)){
				vertices[id].setCondition(formulaeName.get(eventName));
			}else
				vertices[id].setCondition(One.instance());
		}


		// SET FORMULAE INTO RESULT GRAPH
		BooleanFormula[] functions = solution.getFunctions();
		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
			{
				BooleanFormula condition;

				char trivial = trivialEncoding(constraints, m, i, j);
				if (trivial != '?')
				{
					if (trivial == '1')
					{
						condition = One.instance();
					}
					else
					{
						continue;
					}
				}
				/*else
				{
					String constraint = generateConstraint(constraints, m, i, j);
					condition = functions[task.get(constraint)];
				}*/

				/*if (i == j)
				{
					vertices[i].setCondition(condition);
				}*/
				if (i != j)
				{
					VisualArc arc = cpog.connect(vertices[i], vertices[j]);
					String arcName = vertices[i].getLabel() + "->" + vertices[j].getLabel();

					if(formulaeName.containsKey(arcName)){
						condition = formulaeName.get(arcName);
					}else
						condition = One.instance();

					arc.setCondition(condition);
				}
			}

		we.saveMemento();
	}

	public void setSettings(EncoderSettings settings) {
		this.settings = settings;
	}
}
