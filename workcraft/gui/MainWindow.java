package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.StandardBorderManager;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.AlphaPreview;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.event.DockingListener;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants.TabContentPaneBorderKind;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.Framework;
import org.workcraft.framework.ModelSaveFailedException;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.framework.workspace.WorkspaceEntry;
import org.workcraft.gui.edit.graph.GraphEditorPane;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.graph.VisualVertex;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;


public class MainWindow extends JFrame implements DockingConstants{
	private static final long serialVersionUID = 1L;
	public ActionListener defaultActionListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			MainWindow.this.framework.execJavaScript(e.getActionCommand());
		}
	};

	Framework framework;
	public WorkspaceWindow getWorkspaceView() {
		return this.workspaceView;
	}

	WorkspaceWindow workspaceView;
	OutputView outputView;
	ErrorView errorView;
	JavaScriptView jsView;
	PropertyView propertyView;
	ToolboxView toolboxView;
	// MDIPane content;



	JPanel content;

	DefaultDockingPort rootDockingPort;
	Dockable outputDockable;
	Dockable lastEditorDockable;

	InternalWindow testDoc;

	GraphEditorPane editorInFocus;

	private JMenuBar menuBar;

	protected void createViews() {
		this.workspaceView  = new WorkspaceWindow(this.framework);
		this.framework.getWorkspace().addListener(this.workspaceView);
		this.workspaceView.setVisible(true);
		this.propertyView = new PropertyView(this.framework);

		this.outputView = new OutputView(this.framework);
		this.errorView = new ErrorView(this.framework);
		this.jsView = new JavaScriptView(this.framework);

		this.toolboxView = new ToolboxView(this.framework);

		this.lastEditorDockable = null;
		this.outputDockable = null;
		this.editorInFocus = null;
	}

	public MainWindow(final Framework framework) {
		super();
		this.framework = framework;

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				framework.shutdown();
			}
		});

	}

	public void setLAF(String laf) {
		if (JOptionPane.showConfirmDialog(this, "Changing Look and Feel requires GUI restart.\n\n" +
				"This will not affect the workspace (i.e. open documents will stay open),\n" +
				"but the visual editor windows will be closed.\n\nProceed?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		if (laf == null)
			laf = UIManager.getSystemLookAndFeelClassName();

		this.framework.setConfigVar("gui.lookandfeel", laf);
		this.framework.restartGUI();
	}


	protected void attachDockableListener(Dockable dock) {
		dock.addDockingListener( new DockingListener() {

			public void dockingComplete(DockingEvent arg0) {
				for (Object d: arg0.getNewDockingPort().getDockables()) {
					Component comp = ((Dockable)d).getComponent();
					if ( comp instanceof DockableView) {
						DockableView wnd = (DockableView)comp;
						boolean inTab = arg0.getDockable().getComponent().getParent() instanceof JTabbedPane;
						System.out.println(inTab);
						wnd.setStandalone(!inTab);
					}
				}

				for (Object d: arg0.getOldDockingPort().getDockables()) {
					Component comp = ((Dockable)d).getComponent();
					if ( comp instanceof DockableView) {
						DockableView wnd = (DockableView)comp;
						boolean inTab = arg0.getDockable().getComponent().getParent() instanceof JTabbedPane;
						System.out.println(inTab);
						wnd.setStandalone(!inTab);
					}
				}
			}


			public void dragStarted(DockingEvent arg0) {
			}


			public void dropStarted(DockingEvent arg0) {
			}


			public void undockingComplete(DockingEvent arg0) {
			}


			public void undockingStarted(DockingEvent arg0) {
			}


			public void dockingCanceled(DockingEvent evt) {
			}
		});

	}

	public Dockable addView(JComponent view, String name, String region, float split) {
		DockableView dock = new DockableView(name, view);
		Dockable dockable = DockingManager.registerDockable(dock, name);

		this.rootDockingPort.dock(dockable, region);
		DockingManager.setSplitProportion(dockable, split);

		for (Object d: dockable.getDockingPort().getDockables()) {
			Component comp = ((Dockable)d).getComponent();
			if ( comp instanceof DockableView) {
				DockableView wnd = (DockableView)comp;
				boolean inTab = comp.getParent() instanceof JTabbedPane;
				//	System.out.println(inTab);
				wnd.setStandalone(!inTab);
			}
		}

		attachDockableListener(dockable);
		return dockable;
	}

	public Dockable addView(JComponent view, String name, Dockable neighbour) {
		DockableView dock = new DockableView(name, view);
		Dockable dockable = DockingManager.registerDockable(dock, name);

		neighbour.dock(dockable, DockingManager.CENTER_REGION);

		for (Object d: dockable.getDockingPort().getDockables()) {
			Component comp = ((Dockable)d).getComponent();
			if ( comp instanceof DockableView) {
				DockableView wnd = (DockableView)comp;
				boolean inTab = comp.getParent() instanceof JTabbedPane;
				//	System.out.println(inTab);
				wnd.setStandalone(!inTab);
			}
		}

		attachDockableListener(dockable);
		return dockable;

	}

	public Dockable addView(JComponent view, String name, Dockable neighbour, String relativeRegion, float split) {
		DockableView dock = new DockableView(name, view);
		Dockable dockable = DockingManager.registerDockable(dock, name);

		//attachDockableListener(dock);
		DockingManager.dock(dockable, neighbour, relativeRegion);
		DockingManager.setSplitProportion(dockable, split);

		for (Object d: neighbour.getDockingPort().getDockables()) {
			Component comp = ((Dockable)d).getComponent();
			if ( comp instanceof DockableView) {
				DockableView wnd = (DockableView)comp;
				boolean inTab = comp.getParent() instanceof JTabbedPane;
				//		System.out.println(inTab);
				wnd.setStandalone(!inTab);
			}
		}

		attachDockableListener(dockable);
		return dockable;
	}

	public void addEditorView(WorkspaceEntry we) {
		if (we.getModel() == null) {
			JOptionPane.showMessageDialog(this, "The selected entry is not a Workcraft model, and cannot be edited.", "Cannot open editor", JOptionPane.ERROR_MESSAGE);
			return;
		}

		VisualModel visualModel = we.getModel().getVisualModel();

		if (visualModel == null) {
			if (JOptionPane.showConfirmDialog(this, "The selected model does not have visual layout information. Do you want to create a default layout?",
					"No layout information", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
				try {
					visualModel = (VisualModel)PluginManager.createVisualClassFor(we.getModel().getMathModel(), VisualModel.class);
					we.setModel(visualModel);

				} catch (VisualModelConstructionException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error creating visual model", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		}

		GraphEditorPane editor = new GraphEditorPane(this, we);
		String dockableTitle = visualModel.getTitle() + " - " + visualModel.getDisplayName();
		Dockable dockable;

		if (lastEditorDockable == null) {
			dockable = addView (editor, dockableTitle, outputDockable, NORTH_REGION, 0.8f);
		} else {
			dockable = addView (editor, dockableTitle, lastEditorDockable);
		}

		requestFocus(editor);

		lastEditorDockable = dockable;
	}

	public void startup() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
		//SwingUtilities.updateComponentTreeUI(MainWindow.this);

		String laf = this.framework.getConfigVar("gui.lookandfeel");
		if (laf == null)
			laf = UIManager.getCrossPlatformLookAndFeelClassName();
		LAF.setLAF(laf);

		this.content = new JPanel(new BorderLayout(0,0));

		this.rootDockingPort = new DefaultDockingPort();
		// rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));
		this.content.add(this.rootDockingPort, BorderLayout.CENTER);

		setContentPane(this.content);

		boolean maximised = Boolean.parseBoolean(this.framework.getConfigVar("gui.main.maximised"));
		String w = this.framework.getConfigVar("gui.main.width");
		String h = this.framework.getConfigVar("gui.main.height");
		int width = (w==null)?800:Integer.parseInt(w);
		int height = (h==null)?600:Integer.parseInt(h);

		this.setSize(width, height);

		if (maximised)
			setExtendedState(MAXIMIZED_BOTH);

		this.menuBar = new MainMenu(this);
		setJMenuBar(this.menuBar);

		setTitle("Workcraft " + Framework.FRAMEWORK_VERSION_MAJOR+"."+Framework.FRAMEWORK_VERSION_MINOR);

		createViews();

		this.outputView.captureStream();
		this.errorView.captureStream();


		this.rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));

		try {
			new VisualPetriNet(new PetriNet(this.framework));
		} catch (VisualModelConstructionException e) {
			e.printStackTrace();
		}


		//addView (new GraphEditorPane(vpn), "Document 1", DockingManager.CENTER_REGION, 0.5f);
		//addView (new GraphEditorPane(vpn), "Document 2", DockingManager.CENTER_REGION, 0.5f);

		VisualGraph gr = null;
		try {
			Graph g = new Graph(this.framework);

			g.addComponent(new Vertex());
			g.addComponent(new Vertex());
			gr = new VisualGraph(g);
		} catch (Exception e) {
			e.printStackTrace();
		}

		outputDockable = addView (this.outputView, "Output", DockingManager.SOUTH_REGION, 0.8f);
		addView (this.errorView, "Problems", outputDockable);
		addView (this.jsView, "JavaScript", outputDockable);

		Dockable wsvd = addView (this.workspaceView, "Workspace", DockingManager.EAST_REGION, 0.8f);
		addView (this.propertyView, "Property Editor", wsvd, DockingManager.NORTH_REGION, 0.5f);
		addView (this.toolboxView, "Editor Tools", wsvd, DockingManager.NORTH_REGION, 0.5f);

		VisualVertex vv = new VisualVertex(new Vertex());
		gr.getRoot().add(vv);

		DockingManager.display(outputDockable);
		EffectsManager.setPreview(new AlphaPreview(Color.BLACK, Color.GRAY, 0.5f));

		this.workspaceView.startup();

		setVisible(true);

	}

	public ActionListener getDefaultActionListener() {
		return this.defaultActionListener;
	}

	public void shutdown() {
		this.framework.setConfigVar("gui.main.maximised", Boolean.toString((getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0) );
		this.framework.setConfigVar("gui.main.width", Integer.toString(getWidth()));
		this.framework.setConfigVar("gui.main.height", Integer.toString(getHeight()));

		this.outputView.releaseStream();
		this.errorView.releaseStream();

		this.workspaceView.shutdown();

		setVisible(false);
	}

	public void createWork() {
		CreateWorkDialog dialog = new CreateWorkDialog(MainWindow.this);
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1) {
			PluginInfo info = dialog.getSelectedModel();
			try {
				MathModel mathModel = (MathModel)this.framework.getPluginManager().getInstance(info, MathModel.class);

				if (!dialog.getModelTitle().isEmpty())
					mathModel.setTitle(dialog.getModelTitle());

				if (dialog.createVisualSelected()) {
					VisualModel visualModel = (VisualModel)PluginManager.createVisualClassFor(mathModel, VisualModel.class);
					WorkspaceEntry we = this.framework.getWorkspace().add(visualModel);
					if (dialog.openInEditorSelected())
						addEditorView (we);
					//rootDockingPort.dock(new GraphEditorPane(visualModel), CENTER_REGION);
					//addView(new GraphEditorPane(visualModel), mathModel.getTitle() + " - " + mathModel.getDisplayName(), DockingManager.NORTH_REGION, 0.8f);
				} else
					this.framework.getWorkspace().add(mathModel);
			} catch (PluginInstantiationException e) {
				System.err.println(e.getMessage());
			} catch (VisualModelConstructionException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public void requestFocus (GraphEditorPane sender) {
		if (editorInFocus != null)
			editorInFocus.removeFocus();

		sender.grantFocus();
		editorInFocus = sender;
		toolboxView.setToolsForModel(editorInFocus.getModel());
		framework.deleteJavaScriptProperty("_vmodel", framework.getJavaScriptGlobalScope());
		framework.setJavaScriptProperty("_vmodel", sender.getModel(), framework.getJavaScriptGlobalScope(), true);
	}

	public ToolboxView getToolboxView() {
		return toolboxView;
	}

	public void save(WorkspaceEntry we) {
		if (we.getFile() == null) {
			saveAs(we);
			return;
		}

		try {
			this.framework.save(we.getModel(), we.getFile().getPath());
			we.setUnsaved(false);
		} catch (ModelSaveFailedException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void saveAs(WorkspaceEntry we) {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(FileFilters.DOCUMENT_FILES);
		if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			if (fc.getSelectedFile().exists())
				if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION))
					path += ".work";

			File f = new File(path);
			if (f.exists());
				if (JOptionPane.showConfirmDialog(this, "The file \"" + f.getName()+"\" already exists. Do you want to overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION)==JOptionPane.NO_OPTION)
					return;
			try {

				this.framework.save(we.getModel(), path);
				we.setFile(fc.getSelectedFile());
				we.setUnsaved(false);
			} catch (ModelSaveFailedException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void repaintCurrentEditor() {
		if (editorInFocus != null)
			editorInFocus.repaint();


	}
}