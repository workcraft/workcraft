package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
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
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.plugins.PluginManager;
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
	private JDesktopPane jDesktopPane = null;

	public ActionListener defaultActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println (e.getActionCommand());
			framework.execJavaScript(e.getActionCommand());
		}
	};

	Framework framework;
	public WorkspaceWindow getWorkspaceView() {
		return workspaceView;
	}

	WorkspaceWindow workspaceView;
	OutputView outputView;
	ErrorView errorView;
	JavaScriptView jsView;
	PropertyView propertyView;
	// MDIPane content;

	JPanel content;
	DefaultDockingPort rootDockingPort;

	InternalWindow testDoc;

	private JMenuBar menuBar;

	protected void createViews() {
		workspaceView  = new WorkspaceWindow(framework);
		framework.getWorkspace().addListener(workspaceView);
		workspaceView.setVisible(true);
		propertyView = new PropertyView(framework);

		outputView = new OutputView(framework);
		errorView = new ErrorView(framework);
		jsView = new JavaScriptView(framework);
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

		framework.setConfigVar("gui.lookandfeel", laf);
		framework.restartGUI();
	}


	protected void attachDockableListener(Dockable dock) {
		dock.addDockingListener( new DockingListener() {
			@Override
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

			@Override
			public void dragStarted(DockingEvent arg0) {
			}

			@Override
			public void dropStarted(DockingEvent arg0) {
			}

			@Override
			public void undockingComplete(DockingEvent arg0) {
			}

			@Override
			public void undockingStarted(DockingEvent arg0) {
			}

			@Override
			public void dockingCanceled(DockingEvent evt) {
			}
		});

	}

	public Dockable addView(JComponent view, String name, String region, float split) {
		DockableView dock = new DockableView(name, view);
		Dockable dockable = DockingManager.registerDockable(dock, name);

		rootDockingPort.dock(dockable, region);
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
		return addView (view, name, neighbour, DockingManager.CENTER_REGION);

	}

	public Dockable addView(JComponent view, String name, Dockable neighbour, String relativeRegion) {
		DockableView dock = new DockableView(name, view);
		Dockable dockable = DockingManager.registerDockable(dock, name);

		//attachDockableListener(dock);
		DockingManager.dock(dockable, neighbour, relativeRegion);

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

	public void startup() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
		//SwingUtilities.updateComponentTreeUI(MainWindow.this);

		String laf = framework.getConfigVar("gui.lookandfeel");
		if (laf == null)
			laf = UIManager.getCrossPlatformLookAndFeelClassName();
		LAF.setLAF(laf);

		content = new JPanel(new BorderLayout(0,0));

		rootDockingPort = new DefaultDockingPort();
		// rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));
		content.add(rootDockingPort, BorderLayout.CENTER);

		this.setContentPane(content);

		boolean maximised = Boolean.parseBoolean(framework.getConfigVar("gui.main.maximised"));
		String w = framework.getConfigVar("gui.main.width");
		String h = framework.getConfigVar("gui.main.height");
		int width = (w==null)?800:Integer.parseInt(w);
		int height = (h==null)?600:Integer.parseInt(h);

		this.setSize(width, height);

		if (maximised) {
			this.setExtendedState(MAXIMIZED_BOTH);
		}

		menuBar = new MainMenu(this);
		this.setJMenuBar(menuBar);

		this.setTitle("Workcraft " + Framework.FRAMEWORK_VERSION_MAJOR+"."+Framework.FRAMEWORK_VERSION_MINOR);

		createViews();

		outputView.captureStream();
		errorView.captureStream();


		rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));

		VisualPetriNet vpn = null;
		try {
			vpn = new VisualPetriNet(new PetriNet(framework));
		} catch (VisualModelConstructionException e) {
			e.printStackTrace();
		}


		//addView (new GraphEditorPane(vpn), "Document 1", DockingManager.CENTER_REGION, 0.5f);
		//addView (new GraphEditorPane(vpn), "Document 2", DockingManager.CENTER_REGION, 0.5f);

		VisualGraph gr = null;
		try {
			Graph g = new Graph(framework);

			g.addComponent(new Vertex());
			g.addComponent(new Vertex());
			gr = new VisualGraph(g);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//addView (new GraphEditorPane(gr), "Graph", DockingManager.CENTER_REGION, 0.5f);

		//addView (new JPanel(), "No open documents", DockingManager.CENTER_REGION, 0.5f);

		Dockable output = addView (outputView, "Output", DockingManager.SOUTH_REGION, 0.8f);
		addView (errorView, "Problems", output);
		addView (jsView, "JavaScript", output);

		Dockable wsvd = addView (workspaceView, "Workspace", DockingManager.EAST_REGION, 0.8f);
		addView (propertyView, "Property Editor", wsvd, DockingManager.NORTH_REGION);

		VisualVertex vv = new VisualVertex(new Vertex());
		gr.getRoot().add(vv);

		DockingManager.display(output);

		//propertyView.setObject(vv);




		EffectsManager.setPreview(new AlphaPreview(Color.BLACK, Color.BLUE, 0.5f));

		//consoleView.startup();
		workspaceView.startup();

		//		DockingManager.getLayoutManager().store();




		//		content.addFrame(consoleView);
		//		content.addFrame(workspaceView);


		/*		content = new JPanel();
		this.setContentPane(content);


		createViews();

		content.addFrame(consoleView);
		content.addFrame(workspaceView);




		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);



		consoleView.startup();
		workspaceView.startup();


		 */
		setVisible(true);

	}

	public ActionListener getDefaultActionListener() {
		return defaultActionListener;
	}

	public void shutdown() {
		framework.setConfigVar("gui.main.maximised", Boolean.toString((getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0) );
		framework.setConfigVar("gui.main.width", Integer.toString(getWidth()));
		framework.setConfigVar("gui.main.height", Integer.toString(getHeight()));

		outputView.releaseStream();
		errorView.releaseStream();

		workspaceView.shutdown();

		setVisible(false);
	}

	public void createWork() {
		CreateWorkDialog dialog = new CreateWorkDialog(MainWindow.this);
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1) {
			PluginInfo info = dialog.getSelectedModel();
			try {
				MathModel mathModel = (MathModel)framework.getPluginManager().getInstance(info, MathModel.class);
				mathModel.setTitle(dialog.getModelTitle());

				if (dialog.createVisualSelected()) {
					VisualModel visualModel = (VisualModel)PluginManager.createVisualClassFor(mathModel, VisualModel.class);
					framework.getWorkspace().add(visualModel);

					if (dialog.openInEditorSelected()) {
						// open in editor
					}
				} else {
					framework.getWorkspace().add(mathModel);
				}

			} catch (PluginInstantiationException e) {
				System.err.println(e.getMessage());
			} catch (VisualModelConstructionException e) {
				System.err.println(e.getMessage());
			}
		}

	}
}