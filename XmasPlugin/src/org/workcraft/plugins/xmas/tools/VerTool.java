package org.workcraft.plugins.xmas.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SyncComponent;
import org.workcraft.plugins.xmas.components.VisualQueueComponent;
import org.workcraft.plugins.xmas.components.VisualSyncComponent;
import org.workcraft.plugins.xmas.gui.SolutionsDialog1;
import org.workcraft.plugins.xmas.gui.SolutionsDialog2;
import org.workcraft.util.FileUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class VerTool extends AbstractTool implements Tool {

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public String getDisplayName() {
		return "Verification";
	}

	private static class qslist {
		String name;
		int chk;

		public qslist(String s1,int n) {
			name = s1;
			chk=n;
		}
	}

	int cnt_syncnodes=0;
	JFrame mainFrame = null;
	static String level="";
	static String display="";
	static String highlight="";
	static String soln="";
	static List<qslist> qslist = new ArrayList<qslist>();

	public void dispose() {
		mainFrame.setVisible(false);
	}

    private static String ProcessArg(String file) {
		   String typ=null;
		   Scanner sc=null;
		   try {
		      sc=new Scanner(new File(file));
		   } catch (FileNotFoundException e) {
		      System.err.println("Error: " + e.getMessage());
		   }
		   String targ="";
		   String larg="";
		   String sarg="";
		   String arg="";
		   int num;
		   while(sc.hasNextLine()) {
		     Scanner line_=new Scanner(sc.nextLine());
		     Scanner nxt=new Scanner(line_.next());
		     String check=nxt.next();
		     String str;
		     if(check.startsWith("trace")) {
		       nxt=new Scanner(line_.next());
		       targ="-t";
		       targ = targ + nxt.next();
		     }
		     else if(check.startsWith("level")) {
		    	 nxt=new Scanner(line_.next());
		    	 larg="-v";
		    	 str = nxt.next();
		    	 level = str;
		    	 if(str.equals("normal")) {
	     	         //System.out.println("Read v1");
		    		 larg = "-v1";
		    	 }
		    	 else if(str.equals("advanced")) {
	     	        //System.out.println("Read v2");
		    		larg = "-v2";
		    	 }
		     }
		     else if(check.startsWith("display")) {
		    	 nxt=new Scanner(line_.next());
		    	 str = nxt.next();
	     	     //System.out.println("strrr=" + str);
	     	     display = str;
		     }
		     else if(check.startsWith("highlight")) {
		    	 nxt=new Scanner(line_.next());
		    	 str = nxt.next();
	     	     //System.out.println("strrr=" + str);
	     	     highlight = str;
		     }
		     else if(check.startsWith("soln")) {
		    	 nxt=new Scanner(line_.next());
		    	 str = nxt.next();
	     	     //System.out.println("solnnnnnnnnnnnnnnnnn=" + str);
	     	     soln = str;
	     	     sarg = " -s" + str;
		     }
		   }
		   arg = targ + " " + larg + sarg;
		   return arg;
    }

    private static String process_loc(String file)
	{
		Scanner sc=null;
		try {
			sc=new Scanner(new File(file));
		} catch (FileNotFoundException e) {
		    System.err.println("Error: " + e.getMessage());
		}
		String str="";
		while(sc.hasNextLine()) {
			String line_ = sc.nextLine();
			//System.out.println(sc.next());
			str = str + line_ + '\n';
		}
		return str;
	}

    private static void process_qsl(String file)
    {
    	qslist.clear();
		Scanner sc=null;
		try {
			sc=new Scanner(new File(file));
		} catch (FileNotFoundException e) {
		    System.err.println("Error: " + e.getMessage());
		}
		while(sc.hasNextLine()) {
		    Scanner line_=new Scanner(sc.nextLine());
		    Scanner nxt=new Scanner(line_.next());
		    String check=nxt.next();
		    nxt=new Scanner(line_.next());
	    	String str = nxt.next();
	    	int num = Integer.parseInt(str);
	    	//System.out.println("qsl " + check + " " + str + " " + num);
			qslist.add(new qslist(check,num));
		}
    }

    private static String process_eq(String file)
	{
		Scanner sc=null;
		try {
			sc=new Scanner(new File(file));
		} catch (FileNotFoundException e) {
		    System.err.println("Error: " + e.getMessage());
		}
		String str="";
		while(sc.hasNextLine()) {
			String line_ = sc.nextLine();
			//System.out.println(sc.next());
			str = str + line_ + '\n';
		}
		return str;
	}

	public int check_type(String s) {

		if(s.contains("DEADLOCK FREE")) {
			return 0;
		}
		else if(s.contains("TRACE FOUND")) {
			return 1;
		}
		else if(s.contains("Local")) {
			return 2;
		}
		return -1;
	}

	public void init_highlight(Xmas xnet,VisualXmas vnet) {
		QueueComponent qc;
		SyncComponent sc;
		VisualQueueComponent vqc;
		VisualSyncComponent vsc;

		for (Node node : vnet.getNodes()) {
			if(node instanceof VisualQueueComponent) {
				vqc=(VisualQueueComponent)node;
		        vqc.setForegroundColor(Color.black);
		    }
			else if(node instanceof VisualSyncComponent) {
				vsc=(VisualSyncComponent)node;
		        vsc.setForegroundColor(Color.black);
		    }
		}
	}

	public void local_highlight(String s,Xmas xnet,VisualXmas vnet) {
		QueueComponent qc;
		SyncComponent sc;
		VisualQueueComponent vqc;
		VisualSyncComponent vsc;

		//System.out.println("s=" + s);
		for(String st : s.split(" |\n")){
		    if(st.startsWith("Q") || st.startsWith("S")){
		        System.out.println(st);
		        for (Node node : vnet.getNodes()) {
					if(node instanceof VisualQueueComponent) {
		            	vqc=(VisualQueueComponent)node;
		            	qc=vqc.getReferencedQueueComponent();
		            	//if(xnet.getName(qc).contains(st)) {
		    			String rstr;
		    			rstr = xnet.getName(qc);
		    			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
			            if(rstr.equals(st)) {
		            		vqc.setForegroundColor(Color.red);
		            	}
		            }
					else if(node instanceof VisualSyncComponent) {
		            	vsc=(VisualSyncComponent)node;
		            	sc=vsc.getReferencedSyncComponent();
		            	//if(xnet.getName(qc).contains(st)) {
		    			String rstr;
		    			rstr = xnet.getName(sc);
		    			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
			            if(rstr.equals(st)) {
		            		vsc.setForegroundColor(Color.red);
		            	}
		            }
				}
		    }
		}
	}

	public void rel_highlight(String s,Xmas xnet,VisualXmas vnet) {
		int typ=0;
		String str="";
		QueueComponent qc;
		SyncComponent sc;
		VisualQueueComponent vqc;
		VisualSyncComponent vsc;

		for(String st : s.split(" |;|\n")) {
			int n=1;
		    //if(st.startsWith("Q")){
		        if(st.contains("->")) {
			        //System.out.println("testst" + st);
		        	typ=0;
		        	for(String st_ : st.split("->")) {
		        		str=st_;
				        //System.out.println("str===" + str);
		        		for(Node node : vnet.getNodes()) {
							if(node instanceof VisualQueueComponent) {
				            	vqc=(VisualQueueComponent)node;
				            	qc=vqc.getReferencedQueueComponent();
				            	//System.out.println("x===" + xnet.getName(qc));
				    			String rstr;
				    			rstr = xnet.getName(qc);
				    			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
				            	if(rstr.equals(str) && typ==0) {
				            		vqc.setForegroundColor(Color.pink);
				            	}
				            }
							else if(node instanceof VisualSyncComponent) {
				            	vsc=(VisualSyncComponent)node;
				            	sc=vsc.getReferencedSyncComponent();
						        //System.out.println("strrr===" + str + ' ' + xnet.getName(sc));
				    			String rstr;
				    			rstr = xnet.getName(sc);
				    			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
				            	if(rstr.equals(str) && typ==0) {
				            		vsc.setForegroundColor(Color.pink);
				            	}
							}
						}
		        	}
		        }
		        else if(st.contains("<-")) {
			        //System.out.println("testst_" + st);
		        	typ=1;
		        	for(String st_ : st.split("<-")) {
		        		str=st_;
				        //System.out.println("str===" + str);
		        		for(Node node : vnet.getNodes()) {
							if(node instanceof VisualQueueComponent) {
				            	vqc=(VisualQueueComponent)node;
				            	qc=vqc.getReferencedQueueComponent();
				    			String rstr;
				    			rstr = xnet.getName(qc);
				    			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
				            	if(rstr.equals(str) && typ==1) {
				            		vqc.setForegroundColor(Color.red);
				            	}
				            }
							else if(node instanceof VisualSyncComponent) {
				            	vsc=(VisualSyncComponent)node;
				            	sc=vsc.getReferencedSyncComponent();
				    			String rstr;
				    			rstr = xnet.getName(sc);
				    			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
				            	if(rstr.equals(str) && typ==1) {
				            		vsc.setForegroundColor(Color.red);
				            	}
							}
						}
		        	}
		        }

		    //}
		}
	}

	public void active_highlight(Xmas xnet,VisualXmas vnet) {
		QueueComponent qc;
		SyncComponent sc;
		VisualQueueComponent vqc;
		VisualSyncComponent vsc;

		for (qslist ql : qslist) {
			if(ql.chk==0) {
        		for(Node node : vnet.getNodes()) {
					if(node instanceof VisualQueueComponent) {
		            	vqc=(VisualQueueComponent)node;
		            	qc=vqc.getReferencedQueueComponent();
		    			String rstr;
		    			rstr = xnet.getName(qc);
		    			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
		            	if(rstr.equals(ql.name)) {
		            		vqc.setForegroundColor(Color.green);
		            	}
					}
					else if(node instanceof VisualSyncComponent) {
			            vsc=(VisualSyncComponent)node;
			            sc=vsc.getReferencedSyncComponent();
		    			String rstr;
		    			rstr = xnet.getName(sc);
		    			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
			            if(rstr.equals(ql.name)) {
			            	vsc.setForegroundColor(Color.green);
			            }
		            }
				}
			}
		}
	}

	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, Xmas.class);
	}

	GraphEditorPanel editor1;
	Graphics2D g;

	public void run(final WorkspaceEntry we) {
        System.out.println("Verifying Model");

        Xmas xnet = (Xmas)we.getModelEntry().getMathModel();
        VisualXmas vnet = (VisualXmas)we.getModelEntry().getVisualModel();

        try {
        	File cpnFile = XmasSettings.getTempVxmCpnFile();
        	File inFile = XmasSettings.getTempVxmInFile();
        	FileUtils.copyFile(cpnFile, inFile);

            ArrayList<String> vxmCommand = new ArrayList<>();
            vxmCommand.add(XmasSettings.getTempVxmCommandFile().getAbsolutePath());
            vxmCommand.add(ProcessArg(XmasSettings.getTempVxmVsettingsFile().getAbsolutePath()));
        	Process vxmProcess = Runtime.getRuntime().exec(vxmCommand.toArray(new String[vxmCommand.size()]));

            String s, str="";
            InputStreamReader inputStreamReader = new InputStreamReader(vxmProcess.getInputStream());
			BufferedReader stdInput = new BufferedReader(inputStreamReader);
            int n=0;
            int test=-1;
            init_highlight(xnet,vnet);
            while ((s = stdInput.readLine()) != null) {
            	if(test==-1) test=check_type(s);
            	if(n>0) str = str + s + '\n';
            	n++;
            	System.out.println(s);
            }
            if(level.equals("advanced")) {
            	System.out.println("LEVEL IS ADVANCED ");
            	File qslFile = XmasSettings.getTempVxmQslFile();
            	process_qsl(qslFile.getAbsolutePath());

            	File equFile = XmasSettings.getTempVxmEquFile();
            	str = process_eq(equFile.getAbsolutePath());
            }
            else if(level.equals("normal") && test==2) {
            	System.out.println("LEVEL IS NORMAL ");
            	File locFile = XmasSettings.getTempVxmLocFile();
    			str = process_loc(locFile.getAbsolutePath());
            }
            if(test>0) {
        		if(display.equals("popup")) {
        			if(!level.equals("advanced")) {
        				SolutionsDialog1 solutionsDialog = new SolutionsDialog1(test,str);
        			} else {
        				SolutionsDialog2 solutionsDialog = new SolutionsDialog2(test,str);
        			}
        		}
            	if(test==2) {
            		if(highlight.equals("local")) {
            			local_highlight(str,xnet,vnet);
            		}
            		else if(highlight.equals("rel")) {
            			rel_highlight(str,xnet,vnet);
                        //System.out.println("str = " + str);
            			active_highlight(xnet,vnet);
            		}
            	}
    		} else if(test==0) {
        		if(display.equals("popup")) {
        			String message = "The system is deadlock-free.";
        			JOptionPane.showMessageDialog(null, message);
        		}
    		}
        } catch (Exception e) {
        	e.printStackTrace();
        }

        //final SolutionsDialog solutionsDialog = new SolutionsDialog("hello", null);
        //GUI.centerAndSizeToParent(solutionsDialog, we.getFramework().getMainWindow());
		//solutionsDialog.setVisible(true);
		final Framework framework = Framework.getInstance();
        for(GraphEditorPanel e : framework.getMainWindow().getEditors(we)) {
        	editor1 = e;
        	g = (Graphics2D)e.getGraphics();
        	for(int i=0;i<25;i++) {
        		//GUI.drawEditorMessage(editor1, g, Color.RED, "hello");
        	}
        }
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Decorator getDecorator(GraphEditor editor) {
		// TODO Auto-generated method stub
		return null;
	}

}