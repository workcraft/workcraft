package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.util.GUI;

public class CreateWorkDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JList workTypeList;
    private JButton okButton;
    private JButton cancelButton;
    private int modalResult;

    public CreateWorkDialog(MainWindow owner) {
        super(owner);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setModal(true);
        setTitle("New work");
        initComponents();
        setMinimumSize(new Dimension(300, 200));
    }

    static class ListElement implements Comparable<ListElement> {
        public ModelDescriptor descriptor;

        ListElement(ModelDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public String toString() {
            return descriptor.getDisplayName();
        }

        @Override
        public int compareTo(ListElement o) {
            return toString().compareTo(o.toString());
        }
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(SizeHelper.getEmptyBorder());
        setContentPane(contentPane);

        JScrollPane modelScroll = new JScrollPane();
        DefaultListModel listModel = new DefaultListModel();
        workTypeList = new JList(listModel);
        workTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workTypeList.setLayoutOrientation(JList.VERTICAL_WRAP);
        workTypeList.setVisibleRowCount(0);
        workTypeList.setBorder(SizeHelper.getEmptyBorder());

        workTypeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (workTypeList.getSelectedIndex() == -1) {
                    okButton.setEnabled(false);
                } else {
                    okButton.setEnabled(true);
                }
            }
        }
        );

        workTypeList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() == 2) && (workTypeList.getSelectedIndex() != -1)) {
                    ok();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });

        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        final Collection<PluginInfo<? extends ModelDescriptor>> modelDescriptors = pm.getPlugins(ModelDescriptor.class);
        ArrayList<ListElement> elements = new ArrayList<>();
        int minLength = 10;
        for (PluginInfo<? extends ModelDescriptor> plugin : modelDescriptors) {
            elements.add(new ListElement(plugin.newInstance()));
            String displayName = plugin.getSingleton().getDisplayName();
            if ((displayName != null) && (displayName.length() > minLength)) {
                minLength = displayName.length();
            }
        }

        Collections.sort(elements);
        for (ListElement element : elements) {
            listModel.addElement(element);
        }

        modelScroll.setViewportView(workTypeList);
        int width = SizeHelper.getBaseFontSize() * minLength;
        int height = SizeHelper.getListRowSize() * elements.size();
        modelScroll.setPreferredSize(new Dimension(width, height));

        int hGap = SizeHelper.getLayoutHGap();
        int vGap = SizeHelper.getLayoutVGap();
        JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, hGap, vGap));

        okButton = GUI.createDialogButton("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(event -> ok());

        cancelButton = GUI.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> cancel());

        buttonsPane.add(okButton);
        buttonsPane.add(cancelButton);
        contentPane.add(modelScroll, BorderLayout.CENTER);
        contentPane.add(buttonsPane, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        getRootPane().registerKeyboardAction(event -> ok(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> cancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void ok() {
        if (okButton.isEnabled()) {
            modalResult = 1;
            setVisible(false);
        }
    }

    private void cancel() {
        if (cancelButton.isEnabled()) {
            modalResult = 0;
            setVisible(false);
        }
    }

    public int getModalResult() {
        return modalResult;
    }

    public ModelDescriptor getSelectedModel() {
        return ((ListElement) workTypeList.getSelectedValue()).descriptor;
    }

}
