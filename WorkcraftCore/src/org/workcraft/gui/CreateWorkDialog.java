package org.workcraft.gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.*;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.shared.CommonFavoriteSettings;
import org.workcraft.util.GUI;

public class CreateWorkDialog extends JDialog {

    class WorkTypeCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!isSelected && (index % 2 != 0)) {
                Color color = getBackground();
                setBackground(Coloriser.colorise(color, Color.LIGHT_GRAY));
                setOpaque(true);
            }
            return this;
        }
    }

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
        workTypeList.setVisibleRowCount(0);
        workTypeList.setBorder(SizeHelper.getEmptyBorder());
        workTypeList.setCellRenderer(new WorkTypeCellRenderer());
        modelScroll.setViewportView(workTypeList);

        workTypeList.addListSelectionListener(event -> {
            if (workTypeList.getSelectedIndex() == -1) {
                okButton.setEnabled(false);
            } else {
                okButton.setEnabled(true);
            }
        });

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

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        JCheckBox favoriteModelsCheckbox = new JCheckBox("Favorite model types only");
        favoriteModelsCheckbox.addActionListener(event -> fillModelList(modelScroll, listModel, !favoriteModelsCheckbox.isSelected()));
        filterPanel.add(favoriteModelsCheckbox);
        favoriteModelsCheckbox.setSelected(CommonFavoriteSettings.getFilterFavorites());

        // Update list of model types
        fillModelList(modelScroll, listModel, !favoriteModelsCheckbox.isSelected());

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

        contentPane.add(filterPanel, BorderLayout.NORTH);
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

    private void fillModelList(JScrollPane modelScroll, DefaultListModel listModel, boolean showAll) {
        Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        Collection<PluginInfo<? extends ModelDescriptor>> plugins = pm.getPlugins(ModelDescriptor.class);
        ArrayList<ListElement> elements = new ArrayList<>();
        int displayNameLength = 10;
        for (PluginInfo<? extends ModelDescriptor> plugin: plugins) {
            ModelDescriptor modelDescriptor = plugin.newInstance();
            String displayName = modelDescriptor.getDisplayName();
            displayNameLength = Math.max(displayNameLength, displayName.length());
            if (showAll || CommonFavoriteSettings.getIsFavorite(displayName)) {
                elements.add(new ListElement(modelDescriptor));
            }
        }

        Collections.sort(elements);
        listModel.clear();
        for (ListElement element: elements) {
            listModel.addElement(element);
        }

        int width = SizeHelper.getBaseFontSize() * displayNameLength;
        int height = SizeHelper.getListRowSize() * plugins.size();
        modelScroll.setPreferredSize(new Dimension(width, height));
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
