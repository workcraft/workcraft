package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import org.flexdock.docking.drag.effects.DefaultPreview;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.AlphaPreview;
import org.flexdock.docking.drag.preview.GhostPreview;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.event.DockingEventHandler;
import org.flexdock.docking.event.DockingListener;
import org.flexdock.docking.props.DockablePropertySet;
import org.flexdock.docking.props.DockingPortPropertySet;
import org.flexdock.docking.props.PropertyManager;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants.TabContentPaneBorderKind;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.gui.edit.graph.GraphEditorPane;
import org.workcraft.gui.edit.graph.GraphEditorWindow;
import org.workcraft.gui.edit.text.TextEditorWindow;
import org.workcraft.gui.workspace.FileFilters;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;


public class MainWindow extends JFrame implements DockingConstants{
	private static final long serialVersionUID = 1L;
	private JDesktopPane jDesktopPane = null;

	Framework framework;
	WorkspaceWindow workspaceView;
	OutputView outputView;
	ErrorView errorView;
	JavaScriptView jsView;
	// MDIPane content;

	JPanel content;
	DefaultDockingPort rootDockingPort;

	InternalWindow testDoc;

	private JMenuBar menuBar;

	public void createViews() {
		workspaceView  = new WorkspaceWindow(framework);
		framework.getWorkspace().addListener(workspaceView);
		workspaceView.setVisible(true);

		outputView = new OutputView(framework);
		errorView = new ErrorView(framework);
		jsView = new JavaScriptView(framework);
	}

	public MainWindow(Framework framework) {
		super();
		this.framework = framework;
	}

	public void setLAF(String laf) {
		if (JOptionPane.showConfirmDialog(this, "Changing Look and Feel requires GUI restart.\n\n" +
				"This will not affect the workspace (i.e. open documents will stay open),\n" +
				"but the visual editor windows will be closed.\n\nProceed?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		if (laf == null)
			laf = UIManager.getSystemLookAndFeelClassName();
		framework.shutdownGUI();
		framework.setConfigVar("gui.lookandfeel", laf);
		framework.startGUI();
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
		SwingUtilities.updateComponentTreeUI(MainWindow.this);

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

		this.setTitle("Workcraft 2dev");

		createViews();

		rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));

		VisualPetriNet vpn = null;
		try {
			vpn = new VisualPetriNet(new PetriNet(framework));
			InternalWindow doc1 = new GraphEditorWindow("Document 1", vpn);
			doc1.setSize(500, 300);
			doc1.setLocation(10, 10);
			//content.addFrame(doc1);
			doc1.setVisible(true);
		} catch (VisualModelConstructionException e) {
			e.printStackTrace();
		}


		addView (new GraphEditorPane(vpn), "Document 1", DockingManager.CENTER_REGION, 0.5f);
		addView (new GraphEditorPane(vpn), "Document 2", DockingManager.CENTER_REGION, 0.5f);


		Dockable output = addView (outputView, "Output", DockingManager.SOUTH_REGION, 0.8f);
		addView (errorView, "Problems", output);
		addView (jsView, "JavaScript", output);

		addView (workspaceView, "Workspace", DockingManager.EAST_REGION, 0.8f);


		EffectsManager.setPreview(new AlphaPreview(Color.BLACK, Color.BLUE, 0.5f));

		//		consoleView.startup();
		workspaceView.startup();



		outputView.captureStream();
		errorView.captureStream();

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
		this.setVisible(true);

	}

	public void shutdown() {
		framework.setConfigVar("gui.lookandfeel", LAF.getCurrentLAF());
		framework.setConfigVar("gui.main.maximised", Boolean.toString((getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0) );
		framework.setConfigVar("gui.main.width", Integer.toString(getWidth()));
		framework.setConfigVar("gui.main.height", Integer.toString(getHeight()));

		//		consoleView.releaseStreams();
		//consoleView.shutdown();
		workspaceView.shutdown();
		setVisible(false);
	}

	public void addToWorkspace() {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.addChoosableFileFilter(FileFilters.DOCUMENT_FILES);
		fc.setFileFilter(fc.getAcceptAllFileFilter());
		if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
			for(File file : fc.getSelectedFiles()) {
				framework.getWorkspace().add(file.getPath());
			}
		}
	}

	public void saveWorkspaceAs() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(FileFilters.WORKSPACE_FILES);
		if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			framework.getWorkspace().save(FileFilters.checkSaveExtension(fc.getSelectedFile().getPath(), FileFilters.WORKSPACE_EXTENSION));
		}
	}

}