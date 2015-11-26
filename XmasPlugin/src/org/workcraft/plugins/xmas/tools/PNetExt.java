package org.workcraft.plugins.xmas.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;


public class PNetExt {

	//private final Framework framework;

	//VisualCircuit circuit;
	//private CheckCircuitTask checkTask;


	private static boolean printoutput=true;


	private static class source {

		   String name1;
		   String name2;

		   public source(String s1,String s2) {
		     name1 = s1;
		     name2 = s2;
		   }
	}

	private static class switch_ {

		   String name1;
		   String name2;

		   public switch_(String s1,String s2) {
		     name1 = s1;
		     name2 = s2;
		   }
	}

	private static class merge_ {

		   String name1;
		   String name2;

		   public merge_(String s1,String s2) {
		     name1 = s1;
		     name2 = s2;
		   }
	}

	private static class fun_ {

		   String name1;

		   public fun_(String s1) {
		     name1 = s1;
		   }
	}


	static List<source> sourcelist = new ArrayList<source>();
	static List<switch_> switchlist = new ArrayList<switch_>();
	static List<merge_> mergelist = new ArrayList<merge_>();
	static List<fun_> funlist = new ArrayList<fun_>();

	private static void initlist() {
		funlist.clear();
		sourcelist.clear();
		switchlist.clear();
		mergelist.clear();
	}


	private static void ReadFile(String file,int syncflag) {
		   String typ=null;
		   String g1=null;
		   String g2=null;
		   ArrayList storeWordList = new ArrayList();
		   Scanner sc=null;
		   try{
		     sc=new Scanner(new File(file));
		   }catch (FileNotFoundException e) { //Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		   }
		   String name;
		   int num;
		   while(sc.hasNextLine()) {
		     Scanner line_=new Scanner(sc.nextLine());
		     Scanner nxt=new Scanner(line_.next());
		     String check=nxt.next();
		     if(check.startsWith("//gen")) {
		       if(check.startsWith("//gensource")) {
		         nxt=new Scanner(line_.next());
		         name=nxt.next();
		         sourcelist.add(new source(name,name));
		         typ="Source";
		       }
		       else if(check.startsWith("//genfunction")) {
		    	 nxt=new Scanner(line_.next());
		         name=nxt.next();
		         funlist.add(new fun_(name));
		         typ="Function";
		       }
		       else if(check.startsWith("//genmerge")) {
		         nxt=new Scanner(line_.next());
		         name=nxt.next();
		         mergelist.add(new merge_(name,name));
		         typ="Merge";
		       }
		       else if(check.startsWith("//genswitch")) {
		         nxt=new Scanner(line_.next());
		         name=nxt.next();
		         switchlist.add(new switch_(name,name));
		         typ="Switch";
		       }
		     }
		   }
	}

	private static void WriteNet(PrintWriter writer, Collection<SourceComponent> src_nodes, Collection<FunctionComponent> fun_nodes) {
		   writer.println("TS");
		   int no=0;
		   for (SourceComponent src_node : src_nodes) {
			 String ls = "";
			 switch (src_node.getMode()) {
			 case MODE_0:
				 ls = "d";
				 //writer.println("d");
				 break;
			 case MODE_1:
				 ls = "t";
				 //writer.println("t");
				 break;
			 case MODE_2:
				 ls = "n";
				 //writer.println("n");
				 break;
			 }
			 writer.println(sourcelist.get(no).name1 + "\"" + src_node.getType() + "\"" + ls);
			 no++;
		   }
		   writer.println("FN");
		   no=0;
		   for (FunctionComponent fun_node : fun_nodes) {
			 writer.println(funlist.get(no).name1 + "\"" + fun_node.getType() + "\"");       //changed from below
			 no++;
		   }
		   writer.println("ST");
		   for (switch_ sw : switchlist) {
		     writer.println(sw.name1 + "\"" + "t");
		   }
		   writer.println("MT");
		   for (merge_ mrg : mergelist) {
		     writer.println(mrg.name1 + "\"" + mrg.name2);
		   }
		   System.out.print("Output written to CPNFile");
	}

	public PNetExt(Collection<SourceComponent> src_nodes, Collection<FunctionComponent> fun_nodes, int syncflag) {

		initlist();
		File pncFile = new File(XmasSettings.getVxmDirectory(), "PNCFile");
	    PrintWriter writer = null;
	    try
	    {
	    	writer = new PrintWriter(pncFile);
	    	File cpnFile = new File(XmasSettings.getVxmDirectory(), "CPNFile");
	    	ReadFile(cpnFile.getAbsolutePath(), syncflag);
	    	WriteNet(writer,src_nodes,fun_nodes);
	    }
	    catch (Exception e)
	    {
	            e.printStackTrace();
	    }
	    finally
	    {
	            if ( writer != null )
	            {
	                writer.close();
	            }
	    }
		System.out.println("");
	}

}