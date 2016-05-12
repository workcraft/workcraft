package org.workcraft.plugins.son;

import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.apache.log4j.PropertyConfigurator;
import org.workcraft.plugins.son.gui.OutputArea;
import org.workcraft.plugins.son.gui.TextAreaAppender;

public class OutputRedirect {

    public static void redirect(int height, int width, String title) {

        JTextArea jTextArea = new JTextArea(height, width);

        JFrame win = new OutputArea(jTextArea, title);

        win.setVisible(true);

        setupLog4JAppender(jTextArea);

    }

    private static void setupLog4JAppender(JTextArea jTextArea) {

        TextAreaAppender.setTextArea(jTextArea);

        Properties logProperties = new Properties();
        logProperties.put("log4j.rootLogger", "INFO, CONSOLE, TEXTAREA");

        logProperties.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender"); // A standard console appender
        logProperties.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        logProperties.put("log4j.appender.CONSOLE.layout.ConversionPattern", "%m%n");

        logProperties.put("log4j.appender.TEXTAREA", "org.workcraft.plugins.son.gui.TextAreaAppender");
        logProperties.put("log4j.appender.TEXTAREA.layout", "org.apache.log4j.PatternLayout");
        logProperties.put("log4j.appender.TEXTAREA.layout.ConversionPattern", "%m%n");

        PropertyConfigurator.configure(logProperties);
    }

}
