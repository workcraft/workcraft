package org.workcraft.plugins.xmas.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.ForkComponent;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.JoinComponent;
import org.workcraft.plugins.xmas.components.MergeComponent;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SinkComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;
import org.workcraft.plugins.xmas.components.SyncComponent;
import org.workcraft.plugins.xmas.components.VisualQueueComponent;
import org.workcraft.plugins.xmas.components.VisualSourceComponent;
import org.workcraft.plugins.xmas.components.VisualSyncComponent;
import org.workcraft.plugins.xmas.components.XmasContact;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class SyncTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Configure Sync";
	}

	@Override
	public String getSection() {
		return "Sync";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, Xmas.class);
	}

	int cnt_syncnodes=0;
	JFrame mainFrame = null;
	JComboBox combob = null;

	public void dispose() {
		mainFrame.setVisible(false);
	}

	public List<String> slist=new ArrayList<String>();

	public void write_output() {
		//JPanel panelmain=mainFrame.getContentPane().get();
		/*for(int no = 0; no < cnt_syncnodes; no = no+1) {
			System.out.println("Sync" + no);
		    String name = (String)combob.getSelectedItem();
			System.out.println(name);
		}*/
		slist.clear();
		for(Component con : mainFrame.getContentPane().getComponents()) {
			if(con instanceof JPanel) {
        		JPanel jp=(JPanel)con;
				for(Component cn : jp.getComponents()) {
	        		JPanel jp_=(JPanel)cn;
					for(Component cn_ : jp_.getComponents()) {
						if(cn_ instanceof JComboBox) {
			        		JComboBox cb=(JComboBox)cn_;
							String str=cb.getSelectedItem().toString();
							//System.out.println("Found " + str);
							if(str.equals("asynchronous")) {
								slist.add(new String("asynchronous"));
							}
							else if(str.equals("mesochronous")) {
								slist.add(new String("mesochronous"));
							}
							else if(str.equals("pausible")) {
								slist.add(new String("pausible"));
							}
						}
					}
				}
			}
		}
	}

	public void store_fields() {
		slist1.clear();
		slist2.clear();
		for(Component con : mainFrame.getContentPane().getComponents()) {
			if(con instanceof JPanel) {
        		JPanel jp=(JPanel)con;
				for(Component cn : jp.getComponents()) {
	        		JPanel jp_=(JPanel)cn;
	        		int n=1;
					for(Component cn_ : jp_.getComponents()) {
						if(cn_ instanceof JTextField) {
			        		JTextField tf=(JTextField)cn_;
							String str=tf.getText().toString();
							if(n==2) {
								slist1.add(new String(str));
							}
							else if(n==3) {
								slist2.add(new String(str));
							}
							n++;
						}
					}
				}
			}
		}
	}

	public void set_fields() {
		for(Component con : mainFrame.getContentPane().getComponents()) {
			if(con instanceof JPanel) {
        		JPanel jp=(JPanel)con;
				for(Component cn : jp.getComponents()) {
	        		JPanel jp_=(JPanel)cn;
	        		int n=1;
	        		String sel="";
					for(Component cn_ : jp_.getComponents()) {
						if(cn_ instanceof JComboBox) {
			        		JComboBox cb=(JComboBox)cn_;
							sel = (String) cb.getSelectedItem();
						}
						else if(cn_ instanceof JTextField) {
			        		JTextField tf=(JTextField)cn_;
							String str=tf.getText().toString();
							if(sel.equals("mesochronous")) {
								if(n==2) {
									tf.setEnabled(false);
								}
								else if(n==3) {
									tf.setEnabled(false);
								}
							}
							else if(sel.equals("asynchronous")) {
								if(n==2) {
									tf.setEnabled(true);
								}
								else if(n==3) {
									tf.setEnabled(true);
								}
							}
							else if(sel.equals("pausible")) {
								if(n==2) {
									tf.setEnabled(true);
								}
								else if(n==3) {
									tf.setEnabled(true);
								}
							}
							n++;
						}
					}
				}
			}
		}
	}

	int loaded = 0;
	public List<Integer> grnums = new ArrayList<Integer>();
	public List<Integer> grnums1 = new ArrayList<Integer>();
	public List<Integer> grnums2 = new ArrayList<Integer>();
	public List<String> slist1;
    public List<String> slist2;

	public void run(WorkspaceEntry we) {
		System.out.println("Running tests");
		final VisualXmas vnet = (VisualXmas)we.getModelEntry().getVisualModel();
		//Circuit cnet = (Circuit)we.getModelEntry().getModel();
		//VisualCircuit vnet = (VisualCircuit)we.getModelEntry().getMathModel();

		cnt_syncnodes=0;
		if(loaded==0) slist1 = new ArrayList<String>();
		if(loaded==0) slist2 = new ArrayList<String>();
		VisualSourceComponent src_v1=null;
		VisualSourceComponent src_v2=null;
		VisualSourceComponent src_v3=null;
		VisualSourceComponent src_v4=null;
		VisualGroup gr1=null;
		VisualGroup gr2=null;
		VisualGroup gr3=null;
		VisualGroup gr4=null;
		for (Node node : vnet.getNodes()) {
            if(node instanceof VisualSourceComponent) {
            	if(src_v1==null) {
        			System.out.println("storing 1");
            		src_v1=(VisualSourceComponent)node;
            		gr1 = vnet.getGroup((VisualComponent) node);
            		System.out.println(gr1);
            		System.out.println(src_v1.getParent());
            		System.out.println(gr1.getParent());
            		Node n = Hierarchy.getNearestAncestor (vnet.getRoot(), VisualGroup.class);
            		if(n instanceof VisualGroup) {
            			VisualGroup v = (VisualGroup)n;
                    	System.out.println(v);
            		}
            		System.out.println();
            	}
            	else if(src_v2==null) {
        			System.out.println("storing 2");
            		src_v2=(VisualSourceComponent)node;
            		gr2 = vnet.getGroup((VisualComponent) node);
            	}
            	else if(src_v3==null) {
        			System.out.println("storing 3");
            		src_v3=(VisualSourceComponent)node;
            		gr3 = vnet.getGroup(src_v3);
            	}
            	else if(src_v4==null) {
        			System.out.println("storing 4");
            		src_v4=(VisualSourceComponent)node;
            		gr4 = vnet.getGroup(src_v4);
            	}
            }
		}
		if(gr1==gr2) {
			System.out.println("gr1==gr2");
		}
		if(gr1==gr3) {
			System.out.println("gr1==gr3");
		}
		if(gr3==gr4) {
			System.out.println("gr3==gr4");
		}


		Xmas cnet = (Xmas)we.getModelEntry().getMathModel();

		//SyncMenu dialog = new SyncMenu();

		VisualSyncComponent sync_node = null;
		SyncComponent sync_node_ = null;
		SourceComponent src_node = null;
		SinkComponent snk_node = null;

		FunctionComponent fun_node = null;
		QueueComponent qu_node = null;

		ForkComponent frk_node = null;
		JoinComponent jn_node = null;

		SwitchComponent sw_node = null;
		MergeComponent mrg_node = null;

		int num_nodes=0;
		Collection<XmasContact> contacts;

		for (Node node : cnet.getNodes()) {
            //System.out.println("Name =" + cnet.getName(node));
			num_nodes++;
		}
		//GEN JSON
		File jsonFile = new File(XmasSettings.getVxmDirectory(), "JsonFile");
	    PrintWriter writer = null;
		try
		{
	    writer = new PrintWriter(jsonFile);
		int cnt_nodes=0;
		int num_outputs=0;
		List<VisualGroup> groups = new ArrayList<VisualGroup>();
		List<VisualComponent> vcomps = new ArrayList<VisualComponent>();
		List<VisualSyncComponent> vscomps = new ArrayList<VisualSyncComponent>();

		for (Node node : vnet.getNodes()) {
			cnt_nodes++;
            if(node instanceof VisualSyncComponent) {
    			cnt_syncnodes++;
    			vscomps.add((VisualSyncComponent)node);
    			if(loaded==0) grnums1.add(0);
    			if(loaded==0) grnums2.add(0);
            }
		}

		//Finds all components inside groups
		int grnum=1;
        for(VisualGroup vg: Hierarchy.getDescendantsOfType(vnet.getRoot(), VisualGroup.class)) {
        	for(VisualComponent vp: vg.getComponents()) {
                vcomps.add(vp);
                if(loaded==0) grnums.add(grnum);
        	}
        	/*for(VisualConnection vc: vg.getConnections()) { //only connections inside group
                System.out.println("Found connections");
        	}*/
        	grnum++;
        }

		//Finds all sync connections + groups
		Collection <VisualConnection> lvc = ((VisualGroup) vnet.getRoot()).getConnections();
		for(VisualConnection vc: lvc) {
			VisualNode vc1 = vc.getFirst();
			VisualNode vc2 = vc.getSecond();
				Node vn1 = vc1.getParent();
				Node vn2 = vc2.getParent();
				/*if(vn1 instanceof VisualSourceComponent) {
					System.out.println("Found v1");
        			VisualGroup gr = vnet.getGroup((VisualComponent) vn1);
        			//groups.add(gr);
				}
				if(vn2 instanceof VisualSourceComponent) {
					System.out.println("Found v2");
        			VisualGroup gr = vnet.getGroup((VisualComponent) vn2);
				}*/
				int gpos1=0;
				int gpos2=0;
				if(!(vn1 instanceof VisualSyncComponent)) {
					gpos1=vcomps.indexOf(vn1);
				}
				if(!(vn2 instanceof VisualSyncComponent)) {
					gpos2=vcomps.indexOf(vn2);
				}
				if(vn1 instanceof VisualSyncComponent) {
					int pos1=vscomps.indexOf(vn1);
					grnums2.set(pos1,grnums.get(gpos2));
					System.out.println("Assign sync_output[" + pos1 + "]" + "group" + grnums.get(gpos2));
					//grnums2.set(pos1,Integer.valueOf(9)); //9
				}
				if(vn2 instanceof VisualSyncComponent) {
					int pos2=vscomps.indexOf(vn2);
					grnums1.set(pos2,grnums.get(gpos1));
					System.out.println("Assign sync_input[" + pos2 + "]" + "group" + grnums.get(gpos1));
				}
		}

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

		String[] choices = {
		         "asynchronous",
		         "mesochronous",
		         "pausible"
		};

		mainFrame = new JFrame("Configure Synchronisation");
		JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain,BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain,BoxLayout.PAGE_AXIS));

		System.out.println("loaded = " + loaded);
        List<JPanel> panellist = new ArrayList<JPanel>();
		for(int no = 0; no < cnt_syncnodes; no = no+1) {
			if(loaded==0) slist1.add(new String("1"));
			if(loaded==0) slist2.add(new String("1"));
			panellist.add(new JPanel());
			panellist.get(panellist.size()-1).add(new JLabel(" Name" + no));
			panellist.get(panellist.size()-1).add(new JTextField("Sync" + no));
			panellist.get(panellist.size()-1).add(new JLabel(" Type "));
			panellist.get(panellist.size()-1).add(combob = new JComboBox(choices));
			combob.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent event) {

	                JComboBox comboBox = (JComboBox) event.getSource();

	                Object selected = comboBox.getSelectedItem();
	                if(selected.toString().equals("mesochronous")) {
	                	set_fields();
	                }
	            }
	        });
			panellist.get(panellist.size()-1).add(new JLabel(" ClkF1  "));
			panellist.get(panellist.size()-1).add(new JTextField(slist1.get(no),10));
			panellist.get(panellist.size()-1).add(new JLabel(" ClkF2  "));
			panellist.get(panellist.size()-1).add(new JTextField(slist2.get(no),10));
		}
		loaded=1;

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

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
				dispose();
            }

        });

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	int no=1;

				dispose();
				write_output();
				store_fields();
			    String gp="";
			    for(VisualGroup vg: Hierarchy.getDescendantsOfType(vnet.getRoot(), VisualGroup.class)) {
		        	for(VisualComponent vp: vg.getComponents()) {
						if(vp instanceof VisualSourceComponent) {
							VisualSourceComponent vsc=(VisualSourceComponent)vp;
							SourceComponent sc=vsc.getReferencedSourceComponent();
						    for(int i=0; i<grnums1.size(); i++) {
						    	if(grnums1.get(i)==no) gp=slist1.get(i);
						    	if(grnums2.get(i)==no) gp=slist2.get(i);
						    }
						    //System.out.println("Found Source = " + " no= " + no + " grp= " + grnums.get(no) + " sl= " + gp);
						    sc.setGp(gp);
						}
						else if(vp instanceof VisualQueueComponent) {
							VisualQueueComponent vsc=(VisualQueueComponent)vp;
							QueueComponent sc=vsc.getReferencedQueueComponent();
						    for(int i=0; i<grnums1.size(); i++) {
						    	if(grnums1.get(i)==no) gp=slist1.get(i);
						    	if(grnums2.get(i)==no) gp=slist2.get(i);
						    }
						    //System.out.println("Found Source = " + " no= " + no + " grp= " + grnums.get(no) + " sl= " + gp);
						    //sc.setGp(Integer.parseInt(gp));
						    sc.setGp(gp);
						}
					}
					no++;
			    }
			    //System.out.println("grnums = " + grnums);
			    //System.out.println("grnums1 = " + grnums1);
			    //System.out.println("grnums2 = " + grnums2);
			    //System.out.println("slist1 = " + slist1);
			    //System.out.println("slist2 = " + slist2);
			    no=0;
			    for (Node node : vnet.getNodes()) {
		            if(node instanceof VisualSyncComponent) {
		            	VisualSyncComponent vsc=(VisualSyncComponent)node;
						SyncComponent sc=vsc.getReferencedSyncComponent();
					    System.out.println("Sync component " + "Sync" + no + " = " + slist.get(no));
					    System.out.println("group1 = " + grnums1.get(no) + " " + "group2 = " + grnums2.get(no));
					    System.out.println("Clk1 = " + slist1.get(no) + " " + "Clk2 = " + slist2.get(no));
						String gp1=slist1.get(no);
						sc.setGp1(gp1);
						String gp2=slist2.get(no);
						sc.setGp2(gp2);
						String typ=slist.get(no);
						sc.setTyp(typ);
						//System.out.println("Found Sync Type ===========" + sc.getTyp());
						//System.out.println("Found Source = " + " no= " + no + " grp= " + grnums.get(no) + " sl= " + gp);
		            }
		            no++;
				}
            }

        });

        mainFrame.pack();
        mainFrame.setVisible(true);

	}

}