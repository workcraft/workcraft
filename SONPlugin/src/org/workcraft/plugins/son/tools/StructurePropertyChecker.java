package org.workcraft.plugins.son.tools;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.OutputRedirect;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.gui.StructureVerifyDialog;
import org.workcraft.plugins.son.verify.SONMainTask;
import org.workcraft.plugins.son.verify.TSONMainTask;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StructurePropertyChecker implements Tool {

	private final Framework framework;
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private Collection<Node> relationErrors = new HashSet<Node>();
	private Collection<ArrayList<Node>> cycleErrors = new HashSet<ArrayList<Node>>();
	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();

	public StructurePropertyChecker(Framework framework){

		this.framework = framework;

	}

	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);

	}

	public String getSection(){
		return "Verification";
	}

	public String getDisplayName(){
		return "Check structure property";
	}

	public void run(WorkspaceEntry we){

		SONModel net=(SONModel)we.getModelEntry().getMathModel();
		VisualSON vnet = (VisualSON)we.getModelEntry().getVisualModel();

		StructureVerifyDialog dialog = new StructureVerifyDialog(framework.getMainWindow(), net);
		GUI.centerToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);

		if (dialog.getRun() == 1){
			//save current workspace
			we.captureMemento();
			//Change connections from block inside to bounding box.
			if(!vnet.connectToBlocks()){
				we.cancelMemento();
				return;
			}
			OutputRedirect.Redirect();
			SONMainTask sonTask = new SONMainTask(dialog.getSetting(), net);
			//Change connections from block bounding box to inside.
			framework.getTaskManager().execute(sonTask, "Verification");
			relationErrors.addAll(sonTask.getRelationErrors());
			groupErrors.addAll(sonTask.getGroupErrors());
			cycleErrors.addAll(sonTask.getCycleErrors());
			we.cancelMemento();
			System.out.println("size!!!!!!!!!!" + relationErrors.size());
			this.errNodesHighlight(true, net);

			TSONMainTask tsonTask = new TSONMainTask(dialog.getSetting(), net);
			framework.getTaskManager().execute(tsonTask, "Verification");

			int err = sonTask.getTotalErrNum() + tsonTask.getTotalErrNum();
			int warning = sonTask.getTotalWarningNum() + tsonTask.getTotalWarningNum();

			logger.info("\n\nVerification-Result : "+ err + " Error(s), " + warning + " Warning(s).");
			//load saved workspace
		}
	}

	private void errNodesHighlight(boolean b, SONModel net){
		System.out.println(relationErrors.toString());
		if(b){
			for(ONGroup group : groupErrors){
				group.setForegroundColor(Color.RED);
			}

			for(Node node : this.relationErrors){
				net.setFillColor(node, SONSettings.getRelationErrColor());
			}

			for (ArrayList<Node> list : this.cycleErrors)
				for (Node node : list)
					net.setForegroundColor(node, SONSettings.getCyclePathColor());
		}
	}

}
