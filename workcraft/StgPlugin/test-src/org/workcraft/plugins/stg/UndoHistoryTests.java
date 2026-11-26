package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

class UndoHistoryTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testNoHistoryWithoutGui() {
        final Framework framework = Framework.getInstance();
        Assertions.assertFalse(framework.isInGuiMode());
        Assertions.assertTrue(EditorCommonSettings.getUndoDepth() > 0);

        Stg stg = new Stg();
        stg.setTitle("original");
        WorkspaceEntry we = framework.createWork(new ModelEntry(new StgDescriptor(), stg), "undo");
        we.saveMemento();
        we.getModelEntry().getMathModel().setTitle("edited");
        // Undo is only available in the GUI, so no snapshot is kept despite the non-zero depth setting
        we.undo();
        Assertions.assertEquals("edited", we.getModelEntry().getMathModel().getTitle());
        framework.closeWork(we);
    }

}
