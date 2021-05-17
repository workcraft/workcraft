package org.workcraft.utils;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

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

    public static Collection<HighlightData> getHighlights(JTextComponent textComponent) {
        Collection<HighlightData> result = new ArrayList<>();
        for (Highlighter.Highlight highlight : textComponent.getHighlighter().getHighlights()) {
            if (highlight.getPainter() instanceof DefaultHighlighter.DefaultHighlightPainter) {
                DefaultHighlighter.DefaultHighlightPainter painter = (DefaultHighlighter.DefaultHighlightPainter) highlight.getPainter();
                result.add(new HighlightData(highlight.getStartOffset(), highlight.getEndOffset(), painter.getColor()));
            }
        }
        return result;
    }

    public static void highlightLines(JTextComponent textComponent, Collection<HighlightData> highlights) {
        for (HighlightData highlight : highlights) {
            HighlightUtils.highlightLines(textComponent, highlight.fromPos, highlight.toPos, highlight.color);
        }
    }

    public static Object highlightLines(JTextComponent textComponent, int fromPos, int toPos, Color color) {
        return highlightText(textComponent, fromPos, toPos, color, false);
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
                return highlighter.addHighlight(
                        Math.max(fromPos, 0),
                        Math.min(toPos, textComponent.getText().length()),
                        painter);
            } catch (BadLocationException e) {
            }
        }
        return null;
    }

}
