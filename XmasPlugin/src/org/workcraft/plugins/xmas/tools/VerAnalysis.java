package org.workcraft.plugins.xmas.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
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
import org.workcraft.util.LogUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class VerAnalysis extends AbstractTool implements Tool {

    private static class qslist {
           String name;
           int chk;

           qslist(String s1,int n) {
             name = s1;
             chk=n;
           }
    }

    int cnt_syncnodes=0;
    int index=1;
    JFrame mainFrame = null;
    static String level="";
    static String display="";
    static String highlight="";
    static String soln="";
    static List<qslist> qslist = new ArrayList<qslist>();

    @Override
    public String getDisplayName() {
        return "Analysis";
    }

    @Override
    public String getSection() {
        return "Verification";
    }

    public void dispose() {
        mainFrame.setVisible(false);
    }

    public List<JRadioButton> rlist=new ArrayList<JRadioButton>();

    private static List<String> ProcessArg(String file,int index) {
           String typ=null;
           Scanner sc=null;
           try {
              sc=new Scanner(new File(file));
           } catch (FileNotFoundException e) {
               LogUtils.logErrorLine(e.getMessage());
           }
           String targ="";
           String larg="";
           String sarg="";
           String aarg="";
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
                  sarg = "-s" + str;
             }
           }
           //System.out.println("aaaaaaaaaaaindex==============" + index);
           aarg = "-a" + index;
              //System.out.println("aaaaaaaaaaaaaaarggggg=" + aarg);
           ArrayList<String> args = new ArrayList<>();
           if ( !targ.isEmpty() ) args.add(targ);
           if ( !larg.isEmpty() ) args.add(larg);
           if ( !sarg.isEmpty() ) args.add(sarg);
           if ( !aarg.isEmpty() ) args.add(aarg);
           return args;
    }

    private static String process_loc(String file)
    {
        Scanner sc=null;
        try {
            sc=new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logErrorLine(e.getMessage());
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
            LogUtils.logErrorLine(e.getMessage());
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
            LogUtils.logErrorLine(e.getMessage());
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

    static List<JCheckBox> jcbn = new ArrayList<JCheckBox>();
    JCheckBox jcb, jcblast;

    void create_panel(List<JPanel> panellist,String file) {
        int no=1;
        String typ=null;
        Scanner sc=null;
        try {
           sc=new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logErrorLine(e.getMessage());
        }
        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            Scanner line_=new Scanner(line);
            if(line.contains("SOLUTION")) {
                if(no>1) {
                    panellist.get(panellist.size()-1).add(jcb=new JCheckBox(""));
                    ItemListener itemListener = new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if(e.getSource() instanceof JCheckBox){
                            JCheckBox sjcb=(JCheckBox) e.getSource();
                            if(sjcb.isSelected()) index = jcbn.indexOf(sjcb) + 1;
                            if(sjcb.isSelected()) System.out.println("indexa==" + index);
                            if(jcblast!=null) jcblast.setSelected(false);
                            jcblast=sjcb;
                            //String name = sjcb.getName();
                            //System.out.println(name);
                            }
                        }
                    };
                    jcb.addItemListener(itemListener);
                    jcbn.add(jcb);
                }
                panellist.add(new JPanel());
                panellist.get(panellist.size()-1).add(new JLabel(" Soln" + no + ": "));
                no++;
            }
            else if(line.contains("Qu")) {
                Scanner nxt=new Scanner(line_.next());
                String check=nxt.next();
                nxt=new Scanner(line_.next());
                panellist.get(panellist.size()-1).add(new JLabel(check));
                panellist.get(panellist.size()-1).add(new JTextField(nxt.next(),1));
            }
            line_.close();
        }
        panellist.get(panellist.size()-1).add(jcb=new JCheckBox(""));

        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getSource() instanceof JCheckBox){
                    JCheckBox sjcb=(JCheckBox) e.getSource();
                    if(sjcb.isSelected()) index = jcbn.indexOf(sjcb) + 1;
                    //if(sjcb.isSelected()) System.out.println("indexb==" + index);
                    if(jcblast!=null) jcblast.setSelected(false);
                    jcblast=sjcb;
                    //String name = sjcb.getName();
                    //System.out.println(name);
                }
            }
        };
        jcb.addItemListener(itemListener);
        jcbn.add(jcb);
    }

    public void run(final WorkspaceEntry we) {
        System.out.println("Analysing Model");

        final Xmas xnet = (Xmas)we.getModelEntry().getMathModel();
        final VisualXmas vnet = (VisualXmas)we.getModelEntry().getVisualModel();

        mainFrame = new JFrame("Analysis");
        JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain,BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain,BoxLayout.PAGE_AXIS));
        List<JPanel> panellist = new ArrayList<JPanel>();

        JPanel panela = new JPanel();
        panela.setLayout(new FlowLayout(FlowLayout.LEFT));
        panela.add(new JLabel(" UNIQUE SOLUTIONS "));
        panela.add(Box.createHorizontalGlue());
        panelmain.add(panela);

        jcbn.clear();
        File solnFile = XmasSettings.getTempVxmSolnFile();
        create_panel(panellist, solnFile.getAbsolutePath());
        for (JPanel plist : panellist) {
            panelmain.add(plist);
        }

        JPanel panelb = new JPanel();
        panelb.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");
        panelb.add(Box.createHorizontalGlue());
        panelb.add(cancelButton);
        panelb.add(okButton);
        panelmain.add(panelb);

        mainFrame.pack();
        mainFrame.setVisible(true);

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int no=1;
                dispose();
                if (index != 0) {
                    try {
                        File cpnFile = XmasSettings.getTempVxmCpnFile();
                        File inFile = XmasSettings.getTempVxmInFile();
                        FileUtils.copyFile(cpnFile, inFile);

                        ArrayList<String> vxmCommand = new ArrayList<>();
                        vxmCommand.add(XmasSettings.getTempVxmCommandFile().getAbsolutePath());
                        vxmCommand.addAll(ProcessArg(XmasSettings.getTempVxmVsettingsFile().getAbsolutePath(), index));
                        ExternalProcessTask.printCommandLine(vxmCommand);
                        Process vxmProcess = Runtime.getRuntime().exec(vxmCommand.toArray(new String[vxmCommand.size()]));

                        String s, str="";
                        InputStreamReader inputStreamReader = new InputStreamReader(vxmProcess.getInputStream());
                        BufferedReader stdInput = new BufferedReader(inputStreamReader);
                        int n=0;
                        int test=-1;
                        init_highlight(xnet,vnet);
                        while ((s = stdInput.readLine()) != null) {
                            //if(n==1) test=check_type(s);
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
                            str = process_eq(equFile.getAbsolutePath()); //testing str assignment - fpb
                        }
                        else if(level.equals("normal") && (test == 2)) {
                            System.out.println("LEVEL IS NORMAL ");
                            File locFile = XmasSettings.getTempVxmLocFile();
                            str = process_loc(locFile.getAbsolutePath());
                        }
                        if (test>0) {
                            if(display.equals("popup")) {
                                if(!level.equals("advanced")) {
                                    SolutionsDialog1 solutionsDialog = new SolutionsDialog1(test,str);
                                }
                                else {
                                    SolutionsDialog2 solutionsDialog = new SolutionsDialog2(test,str);
                                }
                            }
                            if (test==2) {
                                if(highlight.equals("local")) {
                                    local_highlight(str,xnet,vnet);
                                }
                                else if(highlight.equals("rel")) {
                                    rel_highlight(str,xnet,vnet);
                                    active_highlight(xnet,vnet);
                                }
                            }
                        } else if(test==0) {
                            if(display.equals("popup")) {
                                String message = "The system is deadlock-free.";
                                JOptionPane.showMessageDialog(null, message);
                            }
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
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
