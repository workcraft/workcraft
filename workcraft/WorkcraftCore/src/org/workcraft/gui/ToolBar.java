package org.workcraft.gui;

import org.workcraft.gui.actions.ActionButton;
import org.workcraft.gui.actions.ActionToggle;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;

public class ToolBar extends JToolBar {

    private ActionToggle gridToggle;
    private ActionToggle rulerToggle;
    private ActionToggle nameToggle;
    private ActionToggle labelToggle;

    public ToolBar() {
        super("Global");
        addFileButtons();
        addSeparator();
        addEditButtons();
        addSeparator();
        addViewButtons();
        addSeparator();
        addShowToggles();
        addSeparator();
        refreshToggles();
    }

    private void addFileButtons() {
        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-file-create.svg"),
                MainWindowActions.CREATE_WORK_ACTION));

        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-file-open.svg"),
                MainWindowActions.OPEN_WORK_ACTION));

        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-file-save.svg"),
                MainWindowActions.SAVE_WORK_ACTION));
    }

    private void addEditButtons() {
        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-edit-undo.svg"),
                MainWindowActions.EDIT_UNDO_ACTION));

        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-edit-redo.svg"),
                MainWindowActions.EDIT_REDO_ACTION));

        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-edit-copy.svg"),
                MainWindowActions.EDIT_COPY_ACTION));

        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-edit-paste.svg"),
                MainWindowActions.EDIT_PASTE_ACTION));
    }

    private void addViewButtons() {
        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-view-zoom_in.svg"),
                MainWindowActions.VIEW_ZOOM_IN));

        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-view-zoom_out.svg"),
                MainWindowActions.VIEW_ZOOM_OUT));

        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-view-zoom_default.svg"),
                MainWindowActions.VIEW_ZOOM_DEFAULT));

        add(new ActionButton(GuiUtils.createIconFromSVG("images/toolbar-view-zoom_fit.svg"),
                MainWindowActions.VIEW_ZOOM_FIT));
    }

    private void addShowToggles() {
        gridToggle = new ActionToggle(GuiUtils.createIconFromSVG("images/toolbar-toggle-grid.svg"),
                MainWindowActions.TOGGLE_GRID);

        add(gridToggle);

        rulerToggle = new ActionToggle(GuiUtils.createIconFromSVG("images/toolbar-toggle-ruler.svg"),
                MainWindowActions.TOGGLE_RULER);

        add(rulerToggle);

        nameToggle = new ActionToggle(GuiUtils.createIconFromSVG("images/toolbar-toggle-name.svg"),
                MainWindowActions.TOGGLE_NAME);

        add(nameToggle);

        labelToggle = new ActionToggle(GuiUtils.createIconFromSVG("images/toolbar-toggle-label.svg"),
                MainWindowActions.TOGGLE_LABEL);

        add(labelToggle);
    }

    public void refreshToggles() {
        gridToggle.setSelected(EditorCommonSettings.getGridVisibility());
        rulerToggle.setSelected(EditorCommonSettings.getRulerVisibility());
        nameToggle.setSelected(VisualCommonSettings.getNameVisibility());
        labelToggle.setSelected(VisualCommonSettings.getLabelVisibility());
    }

}
