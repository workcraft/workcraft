package org.workcraft.gui.actions;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class ActionUtils {

    public static String getActionTooltip(Action action) {
        String result = action.getText();
        KeyStroke keystroke = action.getKeyStroke();
        if (keystroke != null) {
            result += " (" + getKeystrokeString(keystroke) + ")";

        }
        return result;
    }

    public static String getKeystrokeString(KeyStroke keystroke) {
        String result = "";
        if (keystroke != null) {
            result = InputEvent.getModifiersExText(keystroke.getModifiers());

            switch (keystroke.getKeyEventType()) {
            case KeyEvent.KEY_TYPED:
                if (!result.isEmpty()) {
                    result += "-";
                }
                result += keystroke.getKeyChar();
                break;
            case KeyEvent.KEY_PRESSED:
            case KeyEvent.KEY_RELEASED:
                if (!result.isEmpty()) {
                    result += "-";
                }
                int keyCode = keystroke.getKeyCode();
                result += getKeyString(keyCode);
                break;
            default:
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
        switch (keyCode) {
        // Navigation keys
        case KeyEvent.VK_LEFT: return "Left";
        case KeyEvent.VK_UP: return "Up";
        case KeyEvent.VK_RIGHT: return "Right";
        case KeyEvent.VK_DOWN: return "Down";
        // Extra navigation keys
        case KeyEvent.VK_INSERT: return "Insert";
        case KeyEvent.VK_DELETE: return "Delete";
        case KeyEvent.VK_END: return "End";
        case KeyEvent.VK_HOME: return "Home";
        case KeyEvent.VK_PAGE_UP: return "PgUp";
        case KeyEvent.VK_PAGE_DOWN: return "PgDn";
        // Function keys
        case KeyEvent.VK_F1: return "F1";
        case KeyEvent.VK_F2: return "F2";
        case KeyEvent.VK_F3: return "F3";
        case KeyEvent.VK_F4: return "F4";
        case KeyEvent.VK_F5: return "F5";
        case KeyEvent.VK_F6: return "F6";
        case KeyEvent.VK_F7: return "F7";
        case KeyEvent.VK_F8: return "F8";
        case KeyEvent.VK_F9: return "F9";
        case KeyEvent.VK_F10: return "F10";
        case KeyEvent.VK_F11: return "F11";
        case KeyEvent.VK_F12: return "F12";
        // Symbols
        case KeyEvent.VK_EXCLAMATION_MARK: return "!";
        case KeyEvent.VK_QUOTEDBL: return "\"";
        case KeyEvent.VK_EURO_SIGN: return "€";
        case KeyEvent.VK_DOLLAR: return "$";
        case KeyEvent.VK_CIRCUMFLEX: return "^";
        case KeyEvent.VK_AMPERSAND: return "&";
        case KeyEvent.VK_ASTERISK: return "*";
        case KeyEvent.VK_UNDERSCORE: return "_";
        case KeyEvent.VK_MINUS: return "-";
        case KeyEvent.VK_PLUS: return "+";
        case KeyEvent.VK_EQUALS: return "=";
        case KeyEvent.VK_AT: return "@";
        case KeyEvent.VK_NUMBER_SIGN: return "#";
        case KeyEvent.VK_COLON: return ":";
        case KeyEvent.VK_SEMICOLON: return ";";
        case KeyEvent.VK_COMMA: return ",";
        case KeyEvent.VK_PERIOD: return ".";
        case KeyEvent.VK_SLASH: return "/";
        case KeyEvent.VK_BACK_SLASH: return "\\";
        case KeyEvent.VK_DEAD_TILDE: return "~";
        // Parenthesis and brackets
        case KeyEvent.VK_LEFT_PARENTHESIS: return "(";
        case KeyEvent.VK_RIGHT_PARENTHESIS: return ")";
        case KeyEvent.VK_OPEN_BRACKET: return "[";
        case KeyEvent.VK_CLOSE_BRACKET: return "]";
        case KeyEvent.VK_BRACELEFT: return "{";
        case KeyEvent.VK_BRACERIGHT: return "}";
        case KeyEvent.VK_LESS: return "<";
        case KeyEvent.VK_GREATER: return ">";
        // Formatting keys
        case KeyEvent.VK_SPACE: return "Space";
        case KeyEvent.VK_TAB: return "Tab";
        case KeyEvent.VK_ENTER: return "Enter";
        case KeyEvent.VK_BACK_SPACE: return "Backspace";
        case KeyEvent.VK_ESCAPE: return "Esc";
        }
        return "0x" + Integer.toString(keyCode, 16);
    }

}
