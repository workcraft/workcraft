package org.workcraft.plugins.xmas.tools;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;


public class PNetConv {

	//private final Framework framework;

	//VisualCircuit circuit;
	//private CheckCircuitTask checkTask;


	//ProgressMonitor<? super MpsatChainResult> monitor;


	private static boolean printoutput=true;

	private static class place {

		   String name;
		   int mk;
		   String typ;

		   public place(String s1,int i1,String s2) {
		     name = s1;
		     mk=i1;
		     typ=s2;
		   }

		   //public void addtransitionlist() {
		   //  this.transitionlist = new ArrayList<transition>();
		   //}
	}

	private static class placetransition {

		   String name1;
		   String name2;

		   public placetransition(String s1,String s2) {
		     name1 = s1;
		     name2 = s2;
		   }
	}

	private static class transitionplace {

		   String name1;
		   String name2;

		   public transitionplace(String s1,String s2) {
		     name1 = s1;
		     name2 = s2;
		   }
	}

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

	static List<place> placelist = new ArrayList<place>();
	static List<String> transitionlist = new ArrayList<String>();
	static List<String> transitiontyp = new ArrayList<String>();
	static List<placetransition> placetransitionlist = new ArrayList<placetransition>();
	static List<transitionplace> transitionplacelist = new ArrayList<transitionplace>();
	static List<source> sourcelist = new ArrayList<source>();
	static List<switch_> switchlist = new ArrayList<switch_>();
	static List<merge_> mergelist = new ArrayList<merge_>();

	private static void initlist() {
		placelist.clear();
		transitionlist.clear();
		transitiontyp.clear();
		placetransitionlist.clear();
		transitionplacelist.clear();
		sourcelist.clear();
		switchlist.clear();
		mergelist.clear();
	}

	private static void writeplacelist() {
		   System.out.print("PLACELIST"+'\n');
		   for(int i=0;i<placelist.size();i++) {
		     //System.out.print(placelist.get(i).num + " " + placelist.get(i).name + " " + placelist.get(i).mk + " tlist =");
		     System.out.print("     preset=");
		     System.out.print('\n');
		   }
	}

	private static void ReadFile(String file) {
		   String typ=null;
		   ArrayList storeWordList = new ArrayList();
		   Scanner sc=null;
		   try{
		     sc=new Scanner(new File(file));
		   }catch (FileNotFoundException e) { //Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		   }
		   String name;
		   int num, num_, oldnum=1, mk;
		   place pl;
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
		         typ="Function";
		       }
		       else if(check.startsWith("//genqueue")) {
		         typ="Queue";
		       }
		       else if(check.startsWith("//gensink")) {
		         typ="Sink";
		       }
		       else if(check.startsWith("//genfork")) {
		         typ="Fork";
		       }
		       else if(check.startsWith("//genjoin")) {
		         typ="Join";
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
		     else if(check.startsWith("mark")) {
		         nxt=new Scanner(line_.next());
		         name=nxt.next();
		         nxt=new Scanner(line_.next());
		         check=nxt.next();
		         num=Integer.parseInt(check);
		         placelist.add(new place(name,num,typ));
		     }
		     else if(check.startsWith("p_")) {
		         nxt=new Scanner(line_.next());
		         name=nxt.next();
		         placetransitionlist.add(new placetransition(check,name));
		  //System.out.print(check + " " +  placetransitionlist.get(placetransitionlist.size()-1).name2 + '\n');
		     }
		     else if(check.startsWith("t_")) {
		         nxt=new Scanner(line_.next());
		         name=nxt.next();
		         transitionplacelist.add(new transitionplace(check,name));
		         if(!transitionlist.contains(check)) {
		           transitionlist.add(check);
		           transitiontyp.add(typ);
		         }
		     }
		   }
	}

	private static int findindex(String s) {
		   int index=0;

		   for (place p : placelist) {
		     if(p.name.equals(s)) {
		       break;
		     }
		     index++;
		   }
		   return index;
	}

	private static void WriteNet(PrintWriter writer) {
		   writer.println("PEP");
		   writer.println("PetriBox");
		   writer.println("FORMAT_N2");
		   writer.println("PL");
		   int i=1;
		   int pindex=0, tindex=0;
		   for (place p : placelist) {
		     //writer.println(i++ + "\"" + p.name + "\"0@0M" + p.mk);
		     writer.println(i++ + "\"" + p.name + "\"" + p.typ + "\"0@0M" + p.mk + "m" + p.mk); //fpb
		   }
		   writer.println("TR");
		   i=1;
		   //for (String t : transitionlist) {
		   for (i=0;i<transitionlist.size();i++) {
		     writer.println((i+1) + "\"" + transitionlist.get(i) + "\"" + transitiontyp.get(i) + "\"0@0");
		   }
		   writer.println("TP");
		   for (transitionplace tp : transitionplacelist) {
		     tindex=transitionlist.indexOf(tp.name1);
		     pindex=findindex(tp.name2);
		     //writer.println(tp.name1 + "<" + tp.name2);
		//System.out.print(tp.name1 + " " + (tindex+1) + " < " + tp.name2 + " " + (pindex+1) + '\n');
		     writer.println(++tindex + "<" + ++pindex);
		   }
		   writer.println("PT");
		   for (placetransition pt : placetransitionlist) {
		     pindex=findindex(pt.name1);
		     tindex=transitionlist.indexOf(pt.name2);
		     //writer.println(pt.name1 + ">" + pt.name2);
		     writer.println(++pindex + ">" + ++tindex);
		   }
		   /*writer.println("TY");
		   for (i=0;i<transitionlist.size();i++) {
		     writer.println((i+1) + "\"" + transitionlist.get(i) + "\"" + transitiontyp.get(i));
		   }*/
		System.out.print("Output written to PNConvFile" + '\n');
	}

	public PNetConv() {

		System.out.println("Converting PN");

		initlist();
		//PrintWriter writer = new PrintWriter("c.ll_net", "UTF-8");
		File file = new File("/home/frank/git/PNConvFile");
	    PrintWriter writer = null;
	    try
	    {
	    	writer = new PrintWriter(file);
	    	ReadFile("/home/frank/git/CPNGenFile");
	    	WriteNet(writer);
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