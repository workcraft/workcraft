package org.workcraft.workspace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;

import java.io.ByteArrayOutputStream;

class MementoManagerTests {

    private final int defaultUndoDepth = EditorCommonSettings.getUndoDepth();

    @AfterEach
    void restoreUndoDepth() {
        EditorCommonSettings.setUndoDepth(defaultUndoDepth);
    }

    @Test
    void testBoundedHistory() {
        MementoManager history = new MementoManager(() -> 2);
        Resource oldest = createMemento();
        Resource middle = createMemento();
        Resource newest = createMemento();
        history.pushUndo(oldest);
        history.pushUndo(middle);
        history.pushUndo(newest);

        Assertions.assertSame(newest, history.pullUndo());
        Assertions.assertSame(middle, history.pullUndo());
        Assertions.assertFalse(history.canUndo());
    }

    @Test
    void testNoHistory() {
        MementoManager history = new MementoManager(() -> 0);
        history.pushUndo(createMemento());
        Assertions.assertFalse(history.canUndo());
    }

    @Test
    void testTrimHistory() {
        MementoManager history = new MementoManager(() -> 99);
        Resource oldest = createMemento();
        Resource newest = createMemento();
        Resource redo = createMemento();
        history.pushUndo(oldest);
        history.pushUndo(newest);
        history.pushRedo(redo);

        history.trim(3);
        Assertions.assertTrue(history.canRedo());

        history.trim(1);
        Assertions.assertFalse(history.canRedo());
        Assertions.assertSame(newest, history.pullUndo());
        Assertions.assertFalse(history.canUndo());

        history.pushUndo(newest);
        history.pushRedo(redo);
        history.trim(0);
        Assertions.assertFalse(history.canUndo());
        Assertions.assertFalse(history.canRedo());
    }

    @Test
    void testUndoDepthRange() {
        EditorCommonSettings.setUndoDepth(-1);
        Assertions.assertEquals(0, EditorCommonSettings.getUndoDepth());
        EditorCommonSettings.setUndoDepth(10000);
        Assertions.assertEquals(9999, EditorCommonSettings.getUndoDepth());
    }

    private static Resource createMemento() {
        return new Resource("memento", new ByteArrayOutputStream());
    }

}
