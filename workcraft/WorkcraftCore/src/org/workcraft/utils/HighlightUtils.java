package org.workcraft.utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class HighlightUtils {

    public static class HighlightData {
        public final int fromPos;
        public final int toPos;
        public final Color color;

        HighlightData(int fromPos, int toPos, Color color) {
            this.fromPos = fromPos;
            this.toPos = toPos;
            this.color = color;
        }
    }

    public static void highlightLine(JTextArea textArea, int lineIndex, Color color) {
        try {
            int fromPos = textArea.getLineStartOffset(lineIndex);
            int toPos = textArea.getLineEndOffset(lineIndex);
            highlightText(textArea, fromPos, toPos, color, false);
        } catch (BadLocationException ignored) {
        }
    }

    public static Object highlightText(JTextComponent textComponent, int fromPos, int toPos, Color color) {
        return highlightText(textComponent, fromPos, toPos, color, true);
    }

    private static Object highlightText(JTextComponent textComponent, int fromPos, int toPos, Color color,
            boolean drawsLayeredHighlights) {

        if ((color != null) && (textComponent != null) && (toPos > fromPos)) {
            DefaultHighlighter highlighter = (DefaultHighlighter) textComponent.getHighlighter();
            highlighter.setDrawsLayeredHighlights(drawsLayeredHighlights);
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(color);
            try {
                return highlighter.addHighlight(fromPos, toPos, painter);
            } catch (BadLocationException ignored) {
            }
        }
        return null;
    }

}
