package org.workcraft.gui.actions;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class ActionUtils {

    public static String getActionTooltip(Action action) {
        String result = action.getDescription();
        if (result == null) {
            result = action.getTitle();
            KeyStroke keystroke = action.getKeyStroke();
            if (keystroke != null) {
                result += " (" + getKeystrokeString(keystroke) + ")";

            }
        }
        return result;
    }

    public static String getKeystrokeString(KeyStroke keystroke) {
        String result = "";
        if (keystroke != null) {
            result = InputEvent.getModifiersExText(keystroke.getModifiers());

            switch (keystroke.getKeyEventType()) {
                case KeyEvent.KEY_TYPED -> {
                    if (!result.isEmpty()) {
                        result += "-";
                    }
                    result += keystroke.getKeyChar();
                }
                case KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED -> {
                    if (!result.isEmpty()) {
                        result += "-";
                    }
                    int keyCode = keystroke.getKeyCode();
                    result += getKeyString(keyCode);
                }
                default -> {
                }
            }
        }
        return result;
    }

    public static String getKeyString(int keyCode) {
        // Letters and numbers
        if (((keyCode >= KeyEvent.VK_0) && (keyCode <= KeyEvent.VK_9))
                || ((keyCode >= KeyEvent.VK_A) && (keyCode <= KeyEvent.VK_Z))) {

            return String.valueOf((char) keyCode);
        }

        return switch (keyCode) {
            // Navigation keys
            case KeyEvent.VK_LEFT -> "Left";
            case KeyEvent.VK_UP -> "Up";
            case KeyEvent.VK_RIGHT -> "Right";
            case KeyEvent.VK_DOWN -> "Down";
            // Extra navigation keys
            case KeyEvent.VK_INSERT -> "Insert";
            case KeyEvent.VK_DELETE -> "Delete";
            case KeyEvent.VK_END -> "End";
            case KeyEvent.VK_HOME -> "Home";
            case KeyEvent.VK_PAGE_UP -> "PgUp";
            case KeyEvent.VK_PAGE_DOWN -> "PgDn";
            // Function keys
            case KeyEvent.VK_F1 -> "F1";
            case KeyEvent.VK_F2 -> "F2";
            case KeyEvent.VK_F3 -> "F3";
            case KeyEvent.VK_F4 -> "F4";
            case KeyEvent.VK_F5 -> "F5";
            case KeyEvent.VK_F6 -> "F6";
            case KeyEvent.VK_F7 -> "F7";
            case KeyEvent.VK_F8 -> "F8";
            case KeyEvent.VK_F9 -> "F9";
            case KeyEvent.VK_F10 -> "F10";
            case KeyEvent.VK_F11 -> "F11";
            case KeyEvent.VK_F12 -> "F12";
            // Symbols
            case KeyEvent.VK_EXCLAMATION_MARK -> "!";
            case KeyEvent.VK_QUOTEDBL -> "\"";
            case KeyEvent.VK_EURO_SIGN -> "€";
            case KeyEvent.VK_DOLLAR -> "$";
            case KeyEvent.VK_CIRCUMFLEX -> "^";
            case KeyEvent.VK_AMPERSAND -> "&";
            case KeyEvent.VK_ASTERISK -> "*";
            case KeyEvent.VK_UNDERSCORE -> "_";
            case KeyEvent.VK_MINUS -> "-";
            case KeyEvent.VK_PLUS -> "+";
            case KeyEvent.VK_EQUALS -> "=";
            case KeyEvent.VK_AT -> "@";
            case KeyEvent.VK_NUMBER_SIGN -> "#";
            case KeyEvent.VK_COLON -> ":";
            case KeyEvent.VK_SEMICOLON -> ";";
            case KeyEvent.VK_COMMA -> ",";
            case KeyEvent.VK_PERIOD -> ".";
            case KeyEvent.VK_SLASH -> "/";
            case KeyEvent.VK_BACK_SLASH -> "\\";
            case KeyEvent.VK_DEAD_TILDE -> "~";
            // Parenthesis and brackets
            case KeyEvent.VK_LEFT_PARENTHESIS -> "(";
            case KeyEvent.VK_RIGHT_PARENTHESIS -> ")";
            case KeyEvent.VK_OPEN_BRACKET -> "[";
            case KeyEvent.VK_CLOSE_BRACKET -> "]";
            case KeyEvent.VK_BRACELEFT -> "{";
            case KeyEvent.VK_BRACERIGHT -> "}";
            case KeyEvent.VK_LESS -> "<";
            case KeyEvent.VK_GREATER -> ">";
            // Formatting keys
            case KeyEvent.VK_SPACE -> "Space";
            case KeyEvent.VK_TAB -> "Tab";
            case KeyEvent.VK_ENTER -> "Enter";
            case KeyEvent.VK_BACK_SPACE -> "Backspace";
            case KeyEvent.VK_ESCAPE -> "Esc";
            default -> "0x" + Integer.toString(keyCode, 16);
        };
    }

}
