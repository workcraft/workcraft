/*
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All rights reserved.
 */

package org.workcraft.plugins.mpsat_verification.tasks;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.XmlUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SolutionReader {

    private static final String SOLUTION_ELEMENT = "solution";
    private static final String TRACE_ELEMENT = "trace";
    private static final String STEP_ELEMENT = "step";
    private static final String CONTINUATION_ELEMENT = "continuation";

    private static final String EXECUTION_SUCCESSFUL_ATTRIBUTE = "execution_successful";
    private static final String MESSAGE_ATTRIBUTE = "message";
    private static final String TRANSITION_ATTRIBUTE = "transition";

    private final boolean success;
    private final String message;
    private final List<Solution> solutions;

    public SolutionReader(File file) throws ParserConfigurationException, SAXException, IOException {
        this(new FileInputStream(file));
    }

    public SolutionReader(InputStream is) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(is);
        Element root = doc.getDocumentElement();
        success = XmlUtils.readBooleanAttribute(root, EXECUTION_SUCCESSFUL_ATTRIBUTE, false);
        message = root.getAttribute(MESSAGE_ATTRIBUTE);
        solutions = success ? readSolutions(root) : null;
    }

    private List<Solution> readSolutions(Element root) {
        List<Solution> result = new LinkedList<>();
        for (Element element : XmlUtils.getChildElements(SOLUTION_ELEMENT, root)) {
            result.add(readSolution(element));
        }
        return result;
    }

    private Solution readSolution(Element root) {
        Trace mainTrace = null;
        Trace branchTrace = null;
        int index = 0;
        Collection<Trace> continuations = new ArrayList<>();
        for (Element element : XmlUtils.getChildElements(TRACE_ELEMENT, root)) {
            if (index == 0) {
                mainTrace = readSteps(element);
                for (Element continuationElement : XmlUtils.getChildElements(CONTINUATION_ELEMENT, element)) {
                    continuations.add(readSteps(continuationElement));
                }
            } else {
                branchTrace = readSteps(element);
                break;
            }
            index++;
        }
        String message = root.getAttribute(MESSAGE_ATTRIBUTE);
        return new Solution(mainTrace, branchTrace, message, continuations);
    }

    private Trace readSteps(Element parent) {
        Trace result = new Trace();
        for (Element element : XmlUtils.getChildElements(STEP_ELEMENT, parent)) {
            result.add(element.getAttribute(TRANSITION_ATTRIBUTE));
        }
        return result;
    }

    public List<Solution> getSolutions() {
        return solutions == null ? null : Collections.unmodifiableList(solutions);
    }

    public boolean hasSolutions() {
        return (solutions != null) && !solutions.isEmpty();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
