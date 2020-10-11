package org.workcraft.plugins.dfs.stg;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dfs.ControlRegister.SynchronisationType;
import org.workcraft.plugins.dfs.*;
import org.workcraft.plugins.dfs.DfsSettings.Palette;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.converters.AbstractToStgConverter;
import org.workcraft.plugins.stg.converters.NodeStg;
import org.workcraft.shared.ColorGenerator;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

public class DfsToStgConverter extends AbstractToStgConverter {

    private static final String C_NAME_PREFIX            = "C_";
    private static final String FW_C_NAME_PREFIX         = "fwC_";
    private static final String BW_C_NAME_PREFIX         = "bwC_";
    private static final String M_NAME_PREFIX            = "M_";
    private static final String OR_M_NAME_PREFIX         = "orM_";
    private static final String AND_M_NAME_PREFIX        = "andM_";
    private static final String TRUE_M_NAME_PREFIX       = "trueM_";
    private static final String FALSE_M_NAME_PREFIX      = "falseM_";

    private static final String C_LABEL_PREFIX          = "C(";
    private static final String FW_C_LABEL_PREFIX       = "fwC(";
    private static final String BW_C_LABEL_PREFIX       = "bwC(";
    private static final String M_LABEL_PREFIX          = "M(";
    private static final String OR_M_LABEL_PREFIX       = "orM(";
    private static final String AND_M_LABEL_PREFIX      = "andM(";
    private static final String TRUE_M_LABEL_PREFIX     = "trueM(";
    private static final String FALSE_M_LABEL_PREFIX    = "falseM(";
    private static final String LOW_LEVEL_LABEL_SUFFIX  = ")=0";
    private static final String HIGH_LEVEL_LABEL_SUFFIX = ")=1";

    private Map<VisualLogic, LogicStg> logicMap;
    private Map<VisualRegister, RegisterStg> registerMap;
    private Map<VisualCounterflowLogic, CounterflowLogicStg> counterflowLogicMap;
    private Map<VisualCounterflowRegister, CounterflowRegisterStg> counterflowRegisterMap;
    private Map<VisualControlRegister, BinaryRegisterStg> controlRegisterMap;
    private Map<VisualPushRegister, BinaryRegisterStg> pushRegisterMap;
    private Map<VisualPopRegister, BinaryRegisterStg> popRegisterMap;
    private Color[] tokenColors;

    public DfsToStgConverter(VisualDfs dfs) {
        super(dfs);
    }

    private VisualDfs getDfsModel() {
        return (VisualDfs) getSrcModel();
    }

    @Override
    public Point2D getScale() {
        return new Point2D.Double(6.0, 6.0);
    }

    @Override
    public void convert() {
        VisualDfs dfs = getDfsModel();
        VisualStg stg = getStgModel();
        NamespaceHelper.copyPageStructure(dfs, stg);
        try {
            // Nodes
            for (VisualLogic node : dfs.getVisualLogics()) {
                LogicStg nodeStg = generateLogicStg(node);
                groupComponentStg(nodeStg);
                putLogicStg(node, nodeStg);
            }
            for (VisualRegister node : dfs.getVisualRegisters()) {
                RegisterStg nodeStg = generateRegisterStg(node);
                groupComponentStg(nodeStg);
                putRegisterStg(node, nodeStg);
            }

            for (VisualCounterflowLogic node : dfs.getVisualCounterflowLogics()) {
                CounterflowLogicStg nodeStg = generateCounterflowLogicStg(node);
                groupComponentStg(nodeStg);
                putCounterflowLogicStg(node, nodeStg);
            }
            for (VisualCounterflowRegister node : dfs.getVisualCounterflowRegisters()) {
                CounterflowRegisterStg nodeStg = generateCounterflowRegisterSTG(node);
                groupComponentStg(nodeStg);
                putCounterflowRegisterStg(node, nodeStg);
            }

            for (VisualControlRegister node : dfs.getVisualControlRegisters()) {
                BinaryRegisterStg nodeStg = generateControlRegisterStg(node);
                groupComponentStg(nodeStg);
                putControlRegisterStg(node, nodeStg);
            }
            for (VisualPushRegister node : dfs.getVisualPushRegisters()) {
                BinaryRegisterStg nodeStg = generatePushRegisterStg(node);
                groupComponentStg(nodeStg);
                putPushRegisterStg(node, nodeStg);
            }
            for (VisualPopRegister node : dfs.getVisualPopRegisters()) {
                BinaryRegisterStg nodeStg = generatePopRegisterStg(node);
                groupComponentStg(nodeStg);
                putPopRegisterStg(node, nodeStg);
            }
            // Connections
            for (VisualLogic node : dfs.getVisualLogics()) {
                connectLogicStg(node);
            }
            for (VisualRegister node : dfs.getVisualRegisters()) {
                connectRegisterStg(node);
            }

            for (VisualCounterflowLogic node : dfs.getVisualCounterflowLogics()) {
                connectCounterflowLogicStg(node);
            }
            for (VisualCounterflowRegister node : dfs.getVisualCounterflowRegisters()) {
                connectCounterflowRegisterStg(node);
            }

            for (VisualControlRegister node : dfs.getVisualControlRegisters()) {
                connectControlRegisterStg(node);
            }
            for (VisualPushRegister node : dfs.getVisualPushRegisters()) {
                connectPushRegisterStg(node);
            }
            for (VisualPopRegister node : dfs.getVisualPopRegisters()) {
                connectPopRegisterStg(node);
            }
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
        NamespaceHelper.removeEmptyPages(stg);
    }

    private ColorGenerator createColorGenerator(boolean required) {
        ColorGenerator result = null;
        if (required) {
            if (tokenColors == null) {
                Palette palette = DfsSettings.getTokenPalette();
                if (palette == Palette.GENERATED) {
                    tokenColors = ColorUtils.getHsbPalette(
                            new float[]{0.05f, 0.45f, 0.85f, 0.25f, 0.65f, 0.15f, 0.55f, 0.95f, 0.35f, 0.75f},
                            new float[]{0.50f}, new float[]{0.7f, 0.5f, 0.3f});
                } else {
                    tokenColors = palette.getColors();
                }
            }
            result = new ColorGenerator(tokenColors);
        }
        return result;
    }

    private LogicStg generateLogicStg(VisualLogic node) throws InvalidConnectionException {
        VisualDfs dfs = getDfsModel();
        VisualStg stg = getStgModel();

        String nodeRef = dfs.getMathReference(node);
        String parentRef = NamespaceHelper.getParentReference(nodeRef);
        String nodeName = NamespaceHelper.getReferenceName(nodeRef);
        VisualPage container = stg.getVisualComponentByMathReference(parentRef, VisualPage.class);

        Point2D pos = getComponentPosition(node);
        double x = pos.getX();
        double y = pos.getY();
        Signal.Type type = Signal.Type.INTERNAL;
        ColorGenerator tokenColorGenerator = createColorGenerator(dfs.getPreset(node).size() == 0);

        VisualPlace c0 = stg.createVisualPlace(getLogicStgNodeReference(nodeName, false), container);
        c0.setLabel(C_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        c0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isComputed()) {
            c0.getReferencedComponent().setTokens(1);
        }
        c0.setForegroundColor(node.getForegroundColor());
        c0.setFillColor(node.getFillColor());
        setPosition(c0, x + 2.0, y + 1.0);

        VisualPlace c1 = stg.createVisualPlace(getLogicStgNodeReference(nodeName, true), container);
        c1.setLabel(C_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        c1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isComputed()) {
            c1.getReferencedComponent().setTokens(1);
        }
        c1.setForegroundColor(node.getForegroundColor());
        c1.setFillColor(node.getFillColor());
        setPosition(c1, x + 2.0, y - 1.0);

        Set<Node> preset = new HashSet<>();
        preset.addAll(dfs.getPreset(node, VisualLogic.class));
        preset.addAll(dfs.getPreset(node, VisualRegister.class));
        preset.addAll(dfs.getPreset(node, VisualControlRegister.class));
        preset.addAll(dfs.getPreset(node, VisualPushRegister.class));
        preset.addAll(dfs.getPreset(node, VisualPopRegister.class));
        if (preset.isEmpty()) {
            preset.add(node);
        }
        Map<Node, VisualSignalTransition> cRs = new HashMap<>();
        Map<Node, VisualSignalTransition> cFs = new HashMap<>();
        VisualSignalTransition cR = null;
        VisualSignalTransition cF = null;
        double dy = 0.0;
        for (Node predNode : preset) {
            String signalName = getLogicStgNodeReference(nodeName, null);
            if (cR == null || node.getReferencedComponent().isEarlyEvaluation()) {
                cR = stg.createVisualSignalTransition(signalName, type, SignalTransition.Direction.PLUS, container);

                cR.setTokenColorGenerator(tokenColorGenerator);
                createConsumingArc(c0, cR, false);
                createProducingArc(cR, c1, true);
                setPosition(cR, x - 2.0, y + 1.0 + dy);
            }
            cRs.put(predNode, cR);
            if (cF == null) {
                cF = stg.createVisualSignalTransition(signalName, type, SignalTransition.Direction.MINUS, container);

                createConsumingArc(c1, cF, false);
                createProducingArc(cF, c0, false);
                setPosition(cF, x - 2.0, y - 1.0 - dy);
            }
            cFs.put(predNode, cF);
            dy += 1.0;
        }
        return new LogicStg(c0, c1, cRs, cFs);
    }

    private void connectLogicStg(VisualLogic node) throws InvalidConnectionException {
        LogicStg nodeStg = getLogicStg(node);
        VisualDfs dfs = getDfsModel();
        for (VisualLogic predNode : dfs.getPreset(node, VisualLogic.class)) {
            LogicStg predNodeStg = getLogicStg(predNode);
            createReadArc(predNodeStg.c1, nodeStg.cRs.get(predNode), true);
            createReadArc(predNodeStg.c0, nodeStg.cFs.get(predNode), false);
        }
        for (VisualRegister predNode : dfs.getPreset(node, VisualRegister.class)) {
            RegisterStg predNodeStg = getRegisterStg(predNode);
            createReadArc(predNodeStg.m1, nodeStg.cRs.get(predNode), true);
            createReadArc(predNodeStg.m0, nodeStg.cFs.get(predNode), false);
        }
        for (VisualControlRegister predNode : dfs.getPreset(node, VisualControlRegister.class)) {
            BinaryRegisterStg predNodeStg = getControlRegisterStg(predNode);
            createReadArc(predNodeStg.m1, nodeStg.cRs.get(predNode), true);
            createReadArc(predNodeStg.m0, nodeStg.cFs.get(predNode), false);
        }
        for (VisualPushRegister predNode : dfs.getPreset(node, VisualPushRegister.class)) {
            BinaryRegisterStg predNodeStg = getPushRegisterStg(predNode);
            createReadArc(predNodeStg.tM1, nodeStg.cRs.get(predNode), true);
            createReadArc(predNodeStg.tM0, nodeStg.cFs.get(predNode), false);
        }
        for (VisualPopRegister predNode : dfs.getPreset(node, VisualPopRegister.class)) {
            BinaryRegisterStg predNodeStg = getPopRegisterStg(predNode);
            createReadArc(predNodeStg.m1, nodeStg.cRs.get(predNode), true);
            createReadArc(predNodeStg.m0, nodeStg.cFs.get(predNode), false);
        }
    }

    public LogicStg getLogicStg(VisualLogic logic) {
        return (logicMap == null) ? null : logicMap.get(logic);
    }

    public void putLogicStg(VisualLogic logic, LogicStg stg) {
        if (logicMap == null) {
            logicMap = new HashMap<>();
        }
        logicMap.put(logic, stg);
    }

    private RegisterStg generateRegisterStg(VisualRegister node) throws InvalidConnectionException {
        VisualDfs dfs = getDfsModel();
        VisualStg stg = getStgModel();

        String nodeRef = dfs.getMathReference(node);
        String parentRef = NamespaceHelper.getParentReference(nodeRef);
        String nodeName = NamespaceHelper.getReferenceName(nodeRef);
        VisualPage container = stg.getVisualComponentByMathReference(parentRef, VisualPage.class);

        Point2D pos = getComponentPosition(node);
        double x = pos.getX();
        double y = pos.getY();
        Signal.Type type = Signal.Type.INTERNAL;
        if (dfs.getPreset(node).size() == 0) {
            type = Signal.Type.INPUT;
        } else if (dfs.getPostset(node).size() == 0) {
            type = Signal.Type.OUTPUT;
        }
        ColorGenerator tokenColorGenerator = createColorGenerator(dfs.getPreset(node).size() == 0);

        VisualPlace m0 = stg.createVisualPlace(getRegisterStgNodeReference(nodeName, false), container);
        m0.setLabel(M_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        m0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isMarked()) {
            m0.getReferencedComponent().setTokens(1);
        }
        m0.setForegroundColor(node.getForegroundColor());
        m0.setFillColor(node.getFillColor());
        setPosition(m0, x + 2.0, y + 1.0);

        VisualPlace m1 = stg.createVisualPlace(getRegisterStgNodeReference(nodeName, true), container);
        m1.setLabel(M_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        m1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isMarked()) {
            m1.getReferencedComponent().setTokens(1);
        }
        m1.setTokenColor(node.getTokenColor());
        setPosition(m1, x + 2.0, y - 1.0);

        String signalName = getRegisterStgNodeReference(nodeName, null);
        VisualSignalTransition mR = stg.createVisualSignalTransition(signalName, type, SignalTransition.Direction.PLUS, container);
        mR.setTokenColorGenerator(tokenColorGenerator);
        createConsumingArc(m0, mR, false);
        createProducingArc(mR, m1, true);
        setPosition(mR, x - 2.0, y + 1.0);

        VisualSignalTransition mF = stg.createVisualSignalTransition(signalName, type, SignalTransition.Direction.MINUS, container);
        createConsumingArc(m1, mF, false);
        createProducingArc(mF, m0, false);
        setPosition(mF, x - 2.0, y - 1.0);

        return new RegisterStg(m0, m1, mR, mF);
    }

    private void connectRegisterStg(VisualRegister node) throws InvalidConnectionException {
        VisualDfs dfs = getDfsModel();
        RegisterStg nodeStg = getRegisterStg(node);
        // Preset
        for (VisualLogic predNode : dfs.getPreset(node, VisualLogic.class)) {
            LogicStg predNodeStg = getLogicStg(predNode);
            createReadArc(predNodeStg.c1, nodeStg.mR, true);
            createReadArc(predNodeStg.c0, nodeStg.mF, false);
        }
        // R-preset
        for (VisualRegister rPredNode : dfs.getRPreset(node, VisualRegister.class)) {
            RegisterStg rPredNodeStg = getRegisterStg(rPredNode);
            createReadArc(rPredNodeStg.m1, nodeStg.mR, true);
            createReadArc(rPredNodeStg.m0, nodeStg.mF, false);
        }
        for (VisualCounterflowRegister rPredNode : dfs.getRPreset(node, VisualCounterflowRegister.class)) {
            CounterflowRegisterStg rPredNodeStg = getCounterflowRegisterStg(rPredNode);
            createReadArc(rPredNodeStg.orM1, nodeStg.mR, true);
            createReadArc(rPredNodeStg.orM0, nodeStg.mF, false);
            createReadArc(rPredNodeStg.andM1, nodeStg.mF, false);
            createReadArc(rPredNodeStg.andM0, nodeStg.mR, false);
        }
        for (VisualControlRegister rPredNode : dfs.getRPreset(node, VisualControlRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getControlRegisterStg(rPredNode);
            createReadArc(rPredNodeStg.m1, nodeStg.mR, true);
            createReadArc(rPredNodeStg.m0, nodeStg.mF, false);
        }
        for (VisualPushRegister rPredNode : dfs.getRPreset(node, VisualPushRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getPushRegisterStg(rPredNode);
            createReadArc(rPredNodeStg.tM1, nodeStg.mR, true);
            createReadArc(rPredNodeStg.tM0, nodeStg.mF, false);
        }
        for (VisualPopRegister rPredNode : dfs.getRPreset(node, VisualPopRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getPopRegisterStg(rPredNode);
            createReadArc(rPredNodeStg.m1, nodeStg.mR, true);
            createReadArc(rPredNodeStg.m0, nodeStg.mF, false);
        }
        // R-postset
        for (VisualRegister rSuccNode : dfs.getRPostset(node, VisualRegister.class)) {
            RegisterStg rSuccNodeStg = getRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.mF, false);
            createReadArc(rSuccNodeStg.m0, nodeStg.mR, false);
        }
        for (VisualCounterflowRegister rSuccNode : dfs.getRPostset(node, VisualCounterflowRegister.class)) {
            CounterflowRegisterStg rSuccNodeStg = getCounterflowRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.andM1, nodeStg.mF, false);
            createReadArc(rSuccNodeStg.andM0, nodeStg.mR, false);
        }
        for (VisualControlRegister rSuccNode : dfs.getRPostset(node, VisualControlRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getControlRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.mF, false);
            createReadArc(rSuccNodeStg.m0, nodeStg.mR, false);
        }
        for (VisualPushRegister rSuccNode : dfs.getRPostset(node, VisualPushRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getPushRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.mF, false);
            createReadArc(rSuccNodeStg.m0, nodeStg.mR, false);
        }
        for (VisualPopRegister rSuccNode : dfs.getRPostset(node, VisualPopRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getPopRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.tM1, nodeStg.mF, false);
            createReadArc(rSuccNodeStg.tM0, nodeStg.mR, false);
        }
    }

    public RegisterStg getRegisterStg(VisualRegister register) {
        return (registerMap == null) ? null : registerMap.get(register);
    }

    public void putRegisterStg(VisualRegister register, RegisterStg stg) {
        if (registerMap == null) {
            registerMap = new HashMap<>();
        }
        registerMap.put(register, stg);
    }

    private CounterflowLogicStg generateCounterflowLogicStg(VisualCounterflowLogic node)
            throws InvalidConnectionException {

        VisualDfs dfs = getDfsModel();
        VisualStg stg = getStgModel();

        String nodeRef = dfs.getMathReference(node);
        String parentRef = NamespaceHelper.getParentReference(nodeRef);
        String nodeName = NamespaceHelper.getReferenceName(nodeRef);
        VisualPage container = stg.getVisualComponentByMathReference(parentRef, VisualPage.class);

        Point2D pos = getComponentPosition(node);
        double x = pos.getX();
        double y = pos.getY();
        Signal.Type type = Signal.Type.INTERNAL;
        ColorGenerator presetTokenColorGenerator = createColorGenerator(dfs.getPreset(node).size() == 0);
        ColorGenerator postsetTokenColorGenerator = createColorGenerator(dfs.getPostset(node).size() == 0);

        String fwC0Name = getCounterflowLogicStgNodeReference(nodeName, true, false);
        VisualPlace fwC0 = stg.createVisualPlace(fwC0Name, container);
        fwC0.setLabel(FW_C_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        fwC0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isForwardComputed()) {
            fwC0.getReferencedComponent().setTokens(1);
        }
        fwC0.setForegroundColor(node.getForegroundColor());
        fwC0.setFillColor(node.getFillColor());
        setPosition(fwC0, x + 2.0, y - 2.0);

        String fwC1Name = getCounterflowLogicStgNodeReference(nodeName, true, true);
        VisualPlace fwC1 = stg.createVisualPlace(fwC1Name, container);
        fwC1.setLabel(FW_C_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        fwC1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isForwardComputed()) {
            fwC1.getReferencedComponent().setTokens(1);
        }
        fwC1.setForegroundColor(node.getForegroundColor());
        fwC1.setFillColor(node.getFillColor());
        setPosition(fwC1, x + 2.0, y - 4.0);

        Set<Node> preset = new HashSet<>();
        preset.addAll(dfs.getPreset(node, VisualCounterflowLogic.class));
        preset.addAll(dfs.getPreset(node, VisualCounterflowRegister.class));
        if (preset.isEmpty()) {
            preset.add(node);
        }
        Map<Node, VisualSignalTransition> fwCRs = new HashMap<>();
        Map<Node, VisualSignalTransition> fwCFs = new HashMap<>();
        VisualSignalTransition fwCR = null;
        VisualSignalTransition fwCF = null;
        double dy = 0.0;
        for (Node predNode : preset) {
            if (fwCR == null || node.getReferencedComponent().isForwardEarlyEvaluation()) {
                fwCR = stg.createVisualSignalTransition(FW_C_NAME_PREFIX + nodeName,
                        type, SignalTransition.Direction.PLUS, container);
                fwCR.setTokenColorGenerator(presetTokenColorGenerator);
                createConsumingArc(fwC0, fwCR, false);
                createProducingArc(fwCR, fwC1, true);
                setPosition(fwCR, x - 2.0, y - 2.0 + dy);
            }
            fwCRs.put(predNode, fwCR);
            if (fwCF == null) {
                fwCF = stg.createVisualSignalTransition(FW_C_NAME_PREFIX + nodeName,
                        type, SignalTransition.Direction.MINUS, container);
                createConsumingArc(fwC1, fwCF, false);
                createProducingArc(fwCF, fwC0, false);
                setPosition(fwCF, x - 2.0, y - 4.0 - dy);
            }
            fwCFs.put(predNode, fwCF);
            dy += 1.0;
        }

        String bwC0Name = getCounterflowLogicStgNodeReference(nodeName, false, false);
        VisualPlace bwC0 = stg.createVisualPlace(bwC0Name, container);
        bwC0.setLabel(BW_C_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        bwC0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isBackwardComputed()) {
            bwC0.getReferencedComponent().setTokens(1);
        }
        bwC0.setForegroundColor(node.getForegroundColor());
        bwC0.setFillColor(node.getFillColor());
        setPosition(bwC0, x + 2.0, y + 4.0);

        String bwC1Name = getCounterflowLogicStgNodeReference(nodeName, false, true);
        VisualPlace bwC1 = stg.createVisualPlace(bwC1Name, container);
        bwC1.setLabel(BW_C_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        bwC1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isBackwardComputed()) {
            bwC1.getReferencedComponent().setTokens(1);
        }
        bwC1.setForegroundColor(node.getForegroundColor());
        bwC1.setFillColor(node.getFillColor());
        setPosition(bwC1, x + 2.0, y + 2.0);

        Set<Node> postset = new HashSet<>();
        postset.addAll(dfs.getPostset(node, VisualCounterflowLogic.class));
        postset.addAll(dfs.getPostset(node, VisualCounterflowRegister.class));
        if (postset.isEmpty()) {
            postset.add(node);
        }
        Map<Node, VisualSignalTransition> bwCRs = new HashMap<>();
        Map<Node, VisualSignalTransition> bwCFs = new HashMap<>();
        VisualSignalTransition bwCR = null;
        VisualSignalTransition bwCF = null;
        dy = 0.0;
        for (Node predNode : postset) {
            String signalName = getCounterflowLogicStgNodeReference(nodeName, false, null);
            if (bwCR == null || node.getReferencedComponent().isBackwardEarlyEvaluation()) {
                bwCR = stg.createVisualSignalTransition(signalName, type, SignalTransition.Direction.PLUS, container);
                bwCR.setTokenColorGenerator(postsetTokenColorGenerator);
                createConsumingArc(bwC0, bwCR, false);
                createProducingArc(bwCR, bwC1, false);
                setPosition(bwCR, x - 2.0, y + 4.0 + dy);
            }
            bwCRs.put(predNode, bwCR);
            if (bwCF == null) {
                bwCF = stg.createVisualSignalTransition(signalName, type, SignalTransition.Direction.MINUS, container);
                createConsumingArc(bwC1, bwCF, false);
                createProducingArc(bwCF, bwC0, false);
                setPosition(bwCF, x - 2.0, y + 2.0 - dy);
            }
            bwCFs.put(predNode, bwCF);
            dy += 1.0;
        }

        return new CounterflowLogicStg(fwC0, fwC1, fwCRs, fwCFs, bwC0, bwC1, bwCRs, bwCFs);
    }

    private void connectCounterflowLogicStg(VisualCounterflowLogic node) throws InvalidConnectionException {
        VisualDfs dfs = getDfsModel();
        CounterflowLogicStg nodeStg = getCounterflowLogicStg(node);
        // Preset
        for (VisualCounterflowLogic predNode : dfs.getPreset(node, VisualCounterflowLogic.class)) {
            CounterflowLogicStg predNodeStg = getCounterflowLogicStg(predNode);
            createReadArc(predNodeStg.fwC1, nodeStg.fwCRs.get(predNode), true);
            createReadArc(predNodeStg.fwC0, nodeStg.fwCFs.get(predNode), false);
        }
        for (VisualCounterflowRegister predNode : dfs.getPreset(node, VisualCounterflowRegister.class)) {
            CounterflowRegisterStg predNodeStg = getCounterflowRegisterStg(predNode);
            createReadArc(predNodeStg.orM1, nodeStg.fwCRs.get(predNode), true);
            createReadArc(predNodeStg.orM0, nodeStg.fwCFs.get(predNode), false);
        }
        // Postset
        for (VisualCounterflowLogic predNode : dfs.getPostset(node, VisualCounterflowLogic.class)) {
            CounterflowLogicStg predNodeStg = getCounterflowLogicStg(predNode);
            createReadArc(predNodeStg.bwC1, nodeStg.bwCRs.get(predNode), false);
            createReadArc(predNodeStg.bwC0, nodeStg.bwCFs.get(predNode), false);
        }
        for (VisualCounterflowRegister predNode : dfs.getPostset(node, VisualCounterflowRegister.class)) {
            CounterflowRegisterStg predNodeStg = getCounterflowRegisterStg(predNode);
            createReadArc(predNodeStg.orM1, nodeStg.bwCRs.get(predNode), false);
            createReadArc(predNodeStg.orM0, nodeStg.bwCFs.get(predNode), false);
        }
    }

    public CounterflowLogicStg getCounterflowLogicStg(VisualCounterflowLogic node) {
        return (counterflowLogicMap == null) ? null : counterflowLogicMap.get(node);
    }

    public void putCounterflowLogicStg(VisualCounterflowLogic node, CounterflowLogicStg nodeStg) {
        if (counterflowLogicMap == null) {
            counterflowLogicMap = new HashMap<>();
        }
        counterflowLogicMap.put(node, nodeStg);
    }

    private CounterflowRegisterStg generateCounterflowRegisterSTG(VisualCounterflowRegister node)
            throws InvalidConnectionException {

        VisualDfs dfs = getDfsModel();
        VisualStg stg = getStgModel();

        String nodeRef = dfs.getMathReference(node);
        String parentRef = NamespaceHelper.getParentReference(nodeRef);
        String nodeName = NamespaceHelper.getReferenceName(nodeRef);
        VisualPage container = stg.getVisualComponentByMathReference(parentRef, VisualPage.class);

        Point2D pos = getComponentPosition(node);
        double x = pos.getX();
        double y = pos.getY();
        Signal.Type type = Signal.Type.INTERNAL;
        if (dfs.getPreset(node).size() == 0 || dfs.getPostset(node).size() == 0) {
            type = Signal.Type.INPUT;
        }
        ColorGenerator presetTokenColorGenerator = createColorGenerator(dfs.getPreset(node).size() == 0);
        ColorGenerator postsetTokenColorGenerator = createColorGenerator(dfs.getPostset(node).size() == 0);

        VisualPlace orM0 = stg.createVisualPlace(getCounterflowRegisterStgNodeReference(nodeName, true, false), container);
        orM0.setLabel(OR_M_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        orM0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isOrMarked()) {
            orM0.getReferencedComponent().setTokens(1);
        }
        orM0.setForegroundColor(node.getForegroundColor());
        orM0.setFillColor(node.getFillColor());
        setPosition(orM0, x + 2.0, y - 2.0);

        VisualPlace orM1 = stg.createVisualPlace(getCounterflowRegisterStgNodeReference(nodeName, true, true), container);
        orM1.setLabel(OR_M_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        orM1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isOrMarked()) {
            orM1.getReferencedComponent().setTokens(1);
        }
        orM1.setForegroundColor(node.getForegroundColor());
        orM1.setFillColor(node.getFillColor());
        setPosition(orM1, x + 2.0, y - 4.0);

        String orSignalName = getCounterflowRegisterStgNodeReference(nodeName, true, null);
        VisualSignalTransition orMRfw = stg.createVisualSignalTransition(orSignalName, type, SignalTransition.Direction.PLUS, container);
        orMRfw.setTokenColorGenerator(presetTokenColorGenerator);
        createConsumingArc(orM0, orMRfw, false);
        createProducingArc(orMRfw, orM1, true);
        setPosition(orMRfw, x - 2.0, y - 2.5);

        VisualSignalTransition orMRbw = stg.createVisualSignalTransition(orSignalName, type, SignalTransition.Direction.PLUS, container);
        orMRbw.setTokenColorGenerator(postsetTokenColorGenerator);
        createConsumingArc(orM0, orMRbw, false);
        createProducingArc(orMRbw, orM1, true);
        setPosition(orMRbw, x - 2.0, y - 1.5);

        VisualSignalTransition orMFfw = stg.createVisualSignalTransition(orSignalName, type, SignalTransition.Direction.MINUS, container);
        createConsumingArc(orM1, orMFfw, false);
        createProducingArc(orMFfw, orM0, false);
        setPosition(orMFfw, x - 2.0, y - 4.5);

        VisualSignalTransition orMFbw = stg.createVisualSignalTransition(orSignalName, type, SignalTransition.Direction.MINUS, container);
        createConsumingArc(orM1, orMFbw, false);
        createProducingArc(orMFbw, orM0, false);
        setPosition(orMFbw, x - 2.0, y - 3.5);

        VisualPlace andM0 = stg.createVisualPlace(getCounterflowRegisterStgNodeReference(nodeName, false, false), container);
        andM0.setLabel(AND_M_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        andM0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isAndMarked()) {
            andM0.getReferencedComponent().setTokens(1);
        }
        andM0.setForegroundColor(node.getForegroundColor());
        andM0.setFillColor(node.getFillColor());
        setPosition(andM0, x + 2.0, y + 4.0);

        VisualPlace andM1 = stg.createVisualPlace(getCounterflowRegisterStgNodeReference(nodeName, false, true), container);
        andM1.setLabel(AND_M_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        andM1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isAndMarked()) {
            andM1.getReferencedComponent().setTokens(1);
        }
        andM1.setForegroundColor(node.getForegroundColor());
        andM1.setFillColor(node.getFillColor());
        setPosition(andM1, x + 2.0, y + 2.0);

        String andSignalName = getCounterflowRegisterStgNodeReference(nodeName, false, null);
        VisualSignalTransition andMR = stg.createVisualSignalTransition(andSignalName, type, SignalTransition.Direction.PLUS, container);
        createConsumingArc(andM0, andMR, false);
        createProducingArc(andMR, andM1, false);
        setPosition(andMR, x - 2.0, y + 4.0);

        VisualSignalTransition andMF = stg.createVisualSignalTransition(andSignalName, type, SignalTransition.Direction.MINUS, container);
        createConsumingArc(andM1, andMF, false);
        createProducingArc(andMF, andM0, false);
        setPosition(andMF, x - 2.0, y + 2.0);

        return new CounterflowRegisterStg(orM0, orM1, orMRfw, orMRbw, orMFfw, orMFbw, andM0, andM1, andMR, andMF);
    }

    private void connectCounterflowRegisterStg(VisualCounterflowRegister node) throws InvalidConnectionException {
        VisualDfs dfs = getDfsModel();
        CounterflowRegisterStg nodeStg = getCounterflowRegisterStg(node);

        for (VisualRegister predNode : dfs.getPreset(node, VisualRegister.class)) {
            RegisterStg predNodeStg = getRegisterStg(predNode);
            createReadArc(predNodeStg.m1, nodeStg.orMRfw, true);
            createReadArc(predNodeStg.m1, nodeStg.andMR, false);
            createReadArc(predNodeStg.m0, nodeStg.orMFfw, false);
            createReadArc(predNodeStg.m0, nodeStg.andMF, false);
        }
        for (VisualRegister succNode : dfs.getPostset(node, VisualRegister.class)) {
            RegisterStg succNodeStg = getRegisterStg(succNode);
            createReadArc(succNodeStg.m1, nodeStg.orMRbw, true);
            createReadArc(succNodeStg.m1, nodeStg.andMR, false);
            createReadArc(succNodeStg.m0, nodeStg.orMFbw, false);
            createReadArc(succNodeStg.m0, nodeStg.andMF, false);
        }

        for (VisualCounterflowLogic predNode : dfs.getPreset(node, VisualCounterflowLogic.class)) {
            CounterflowLogicStg predNodeStg = getCounterflowLogicStg(predNode);
            createReadArc(predNodeStg.fwC1, nodeStg.orMRfw, true);
            createReadArc(predNodeStg.fwC0, nodeStg.orMFfw, false);
            createReadArc(predNodeStg.fwC1, nodeStg.andMR, false);
            createReadArc(predNodeStg.fwC0, nodeStg.andMF, false);
        }
        for (VisualCounterflowLogic succNode : dfs.getPostset(node, VisualCounterflowLogic.class)) {
            CounterflowLogicStg succNodeStg = getCounterflowLogicStg(succNode);
            createReadArc(succNodeStg.bwC1, nodeStg.orMRbw, true);
            createReadArc(succNodeStg.bwC0, nodeStg.orMFbw, false);
            createReadArc(succNodeStg.bwC1, nodeStg.andMR, false);
            createReadArc(succNodeStg.bwC0, nodeStg.andMF, false);
        }

        for (VisualCounterflowRegister predNode : dfs.getPreset(node, VisualCounterflowRegister.class)) {
            CounterflowRegisterStg predNodeStg = getCounterflowRegisterStg(predNode);
            createReadArc(predNodeStg.orM1, nodeStg.orMRfw, true);
            createReadArc(predNodeStg.orM0, nodeStg.orMFfw, false);
        }
        for (VisualCounterflowRegister succNode : dfs.getPostset(node, VisualCounterflowRegister.class)) {
            CounterflowRegisterStg succNodeStg = getCounterflowRegisterStg(succNode);
            createReadArc(succNodeStg.orM1, nodeStg.orMRbw, true);
            createReadArc(succNodeStg.orM0, nodeStg.orMFbw, false);
        }

        Set<VisualCounterflowRegister> rSet = new HashSet<>();
        rSet.add(node);
        rSet.addAll(dfs.getRPreset(node, VisualCounterflowRegister.class));
        rSet.addAll(dfs.getRPostset(node, VisualCounterflowRegister.class));
        for (VisualCounterflowRegister rNode : rSet) {
            CounterflowRegisterStg rNodeStg = getCounterflowRegisterStg(rNode);
            createReadArc(rNodeStg.orM1, nodeStg.andMR, true);
            createReadArc(rNodeStg.orM0, nodeStg.andMF, false);
            createReadArc(rNodeStg.andM1, nodeStg.orMFfw, false);
            createReadArc(rNodeStg.andM1, nodeStg.orMFbw, false);
            createReadArc(rNodeStg.andM0, nodeStg.orMRfw, false);
            createReadArc(rNodeStg.andM0, nodeStg.orMRbw, false);
        }
    }

    public CounterflowRegisterStg getCounterflowRegisterStg(VisualCounterflowRegister node) {
        return (counterflowRegisterMap == null) ? null : counterflowRegisterMap.get(node);
    }

    public void putCounterflowRegisterStg(VisualCounterflowRegister node, CounterflowRegisterStg nodeStg) {
        if (counterflowRegisterMap == null) {
            counterflowRegisterMap = new HashMap<>();
        }
        counterflowRegisterMap.put(node, nodeStg);
    }

    private BinaryRegisterStg generateBinaryRegisterSTG(VisualBinaryRegister node, boolean andSync, boolean orSync)
            throws InvalidConnectionException {

        VisualDfs dfs = getDfsModel();
        VisualStg stg = getStgModel();

        String nodeRef = dfs.getMathReference(node);
        String parentRef = NamespaceHelper.getParentReference(nodeRef);
        String nodeName = NamespaceHelper.getReferenceName(nodeRef);
        VisualPage container = stg.getVisualComponentByMathReference(parentRef, VisualPage.class);

        Point2D pos = getComponentPosition(node);
        double x = pos.getX();
        double y = pos.getY();
        Signal.Type type = Signal.Type.INTERNAL;
        if (dfs.getPreset(node, VisualControlRegister.class).size() == 0) {
            type = Signal.Type.INPUT;
        } else if (dfs.getPostset(node).size() == 0) {
            type = Signal.Type.OUTPUT;
        }
        ColorGenerator tokenColorGenerator = createColorGenerator(dfs.getPreset(node).size() == 0);

        VisualPlace m0 = stg.createVisualPlace(getBinaryRegisterStgNodeReference(nodeName, null, false), container);
        m0.setLabel(M_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        m0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isTrueMarked() && !node.getReferencedComponent().isFalseMarked()) {
            m0.getReferencedComponent().setTokens(1);
        }
        m0.setForegroundColor(node.getForegroundColor());
        m0.setFillColor(node.getFillColor());
        setPosition(m0, x - 4.0, y + 1.0);

        VisualPlace m1 = stg.createVisualPlace(getBinaryRegisterStgNodeReference(nodeName, null, true), container);
        m1.setLabel(M_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        m1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isTrueMarked() || node.getReferencedComponent().isFalseMarked()) {
            m1.getReferencedComponent().setTokens(1);
        }
        m1.setForegroundColor(node.getForegroundColor());
        m1.setFillColor(node.getFillColor());
        setPosition(m1, x - 4.0, y - 1.0);

        VisualPlace tM0 = stg.createVisualPlace(getBinaryRegisterStgNodeReference(nodeName, true, false), container);
        tM0.setLabel(TRUE_M_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        tM0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isTrueMarked()) {
            tM0.getReferencedComponent().setTokens(1);
        }
        tM0.setForegroundColor(node.getForegroundColor());
        tM0.setFillColor(node.getFillColor());
        setPosition(tM0, x + 4.0, y - 2.0);

        VisualPlace tM1 = stg.createVisualPlace(getBinaryRegisterStgNodeReference(nodeName, true, true), container);
        tM1.setLabel(TRUE_M_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        tM1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isTrueMarked()) {
            tM1.getReferencedComponent().setTokens(1);
        }
        tM1.setForegroundColor(node.getForegroundColor());
        tM1.setFillColor(node.getFillColor());
        setPosition(tM1, x + 4.0, y - 4.0);

        Set<Node> preset = new HashSet<>(dfs.getRPreset(node, VisualControlRegister.class));
        if (preset.isEmpty()) {
            preset.add(node);
        }

        Map<Node, VisualSignalTransition> tMRs = new HashMap<>();
        VisualSignalTransition tMR = null;
        double dy = 0.0;
        String trueSignalName = getBinaryRegisterStgNodeReference(nodeName, true, null);
        for (Node predNode : preset) {
            if (tMR == null || orSync) {
                tMR = stg.createVisualSignalTransition(trueSignalName, type, SignalTransition.Direction.PLUS, container);
                tMR.setTokenColorGenerator(tokenColorGenerator);
                createConsumingArc(tM0, tMR, false);
                createProducingArc(tMR, tM1, true);
                createConsumingArc(m0, tMR, false);
                createProducingArc(tMR, m1, true);
                setPosition(tMR, x, y - 2.0 + dy);
            }
            tMRs.put(predNode, tMR);
            dy += 1.0;
        }
        VisualSignalTransition tMF = stg.createVisualSignalTransition(trueSignalName, type, SignalTransition.Direction.MINUS, container);
        createConsumingArc(tM1, tMF, false);
        createProducingArc(tMF, tM0, false);
        createConsumingArc(m1, tMF, false);
        createProducingArc(tMF, m0, false);
        setPosition(tMF, x, y - 4.0 - dy);

        VisualPlace fM0 = stg.createVisualPlace(getBinaryRegisterStgNodeReference(nodeName, false, false), container);
        fM0.setLabel(FALSE_M_LABEL_PREFIX + nodeName + LOW_LEVEL_LABEL_SUFFIX);
        fM0.setLabelPositioning(Positioning.BOTTOM);
        if (!node.getReferencedComponent().isFalseMarked()) {
            fM0.getReferencedComponent().setTokens(1);
        }
        fM0.setForegroundColor(node.getForegroundColor());
        fM0.setFillColor(node.getFillColor());
        setPosition(fM0, x + 4.0, y + 4.0);

        VisualPlace fM1 = stg.createVisualPlace(getBinaryRegisterStgNodeReference(nodeName, false, true), container);
        fM1.setLabel(FALSE_M_LABEL_PREFIX + nodeName + HIGH_LEVEL_LABEL_SUFFIX);
        fM1.setLabelPositioning(Positioning.TOP);
        if (node.getReferencedComponent().isFalseMarked()) {
            fM1.getReferencedComponent().setTokens(1);
        }
        fM1.setForegroundColor(node.getForegroundColor());
        fM1.setFillColor(node.getFillColor());
        setPosition(fM1, x + 4.0, y + 2.0);

        Map<Node, VisualSignalTransition> fMRs = new HashMap<>();
        VisualSignalTransition fMR = null;
        dy = 0.0;
        String falseSignalName = FALSE_M_NAME_PREFIX + nodeName;
        for (Node predNode : preset) {
            if (fMR == null || andSync) {
                fMR = stg.createVisualSignalTransition(falseSignalName, type, SignalTransition.Direction.PLUS, container);
                fMR.setTokenColorGenerator(tokenColorGenerator);
                createConsumingArc(fM0, fMR, false);
                createProducingArc(fMR, fM1, true);
                createConsumingArc(m0, fMR, false);
                createProducingArc(fMR, m1, true);
                setPosition(fMR, x, y + 4.0 + dy);
            }
            fMRs.put(predNode, fMR);
            dy += 1.0;
        }
        VisualSignalTransition fMF = stg.createVisualSignalTransition(falseSignalName, type, SignalTransition.Direction.MINUS, container);
        createConsumingArc(fM1, fMF, false);
        createProducingArc(fMF, fM0, false);
        createConsumingArc(m1, fMF, false);
        createProducingArc(fMF, m0, false);
        setPosition(fMF, x, y + 2.0 - dy);

        // Mutual exclusion
        createReadArcs(tM0, fMRs.values(), false);
        createReadArcs(fM0, tMRs.values(), false);

        return new BinaryRegisterStg(m0, m1, tM0, tM1, tMRs, tMF, fM0, fM1, fMRs, fMF);
    }

    private BinaryRegisterStg generateControlRegisterStg(VisualControlRegister node)
            throws InvalidConnectionException {

        boolean andSync = node.getReferencedComponent().getSynchronisationType() == SynchronisationType.AND;
        boolean orSync = node.getReferencedComponent().getSynchronisationType() == SynchronisationType.OR;
        return generateBinaryRegisterSTG(node, andSync, orSync);
    }

    private void connectControlRegisterStg(VisualControlRegister node) throws InvalidConnectionException {
        VisualDfs dfs = getDfsModel();
        BinaryRegisterStg nodeStg = getControlRegisterStg(node);
        // Preset
        for (VisualLogic predNode : dfs.getPreset(node, VisualLogic.class)) {
            LogicStg predNodeStg = getLogicStg(predNode);
            createReadArcs(predNodeStg.c1, nodeStg.tMRs.values(), true);
            createReadArcs(predNodeStg.c1, nodeStg.fMRs.values(), true);
            createReadArc(predNodeStg.c0, nodeStg.tMF, false);
            createReadArc(predNodeStg.c0, nodeStg.fMF, false);
        }
        // R-preset
        for (VisualRegister rPredNode : dfs.getRPreset(node, VisualRegister.class)) {
            RegisterStg rPredNodeStg = getRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.m1, nodeStg.tMRs.values(), true);
            createReadArcs(rPredNodeStg.m1, nodeStg.fMRs.values(), true);
            createReadArc(rPredNodeStg.m0, nodeStg.tMF, false);
            createReadArc(rPredNodeStg.m0, nodeStg.fMF, false);
        }
        Collection<VisualControlRegister> crPreset = dfs.getRPreset(node, VisualControlRegister.class);
        for (VisualControlRegister rPredNode : crPreset) {
            BinaryRegisterStg rPredNodeStg = getControlRegisterStg(rPredNode);
            Connection connection = dfs.getConnection(rPredNode, node);
            if ((connection instanceof VisualControlConnection)
                    && ((VisualControlConnection) connection).getReferencedConnection().isInverting()) {

                createReadArc(rPredNodeStg.tM1, nodeStg.fMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.fM1, nodeStg.tMRs.get(rPredNode), true);
            } else {
                createReadArc(rPredNodeStg.tM1, nodeStg.tMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.fM1, nodeStg.fMRs.get(rPredNode), true);
            }
            if (node.getReferencedComponent().getSynchronisationType() != SynchronisationType.PLAIN) {
                for (VisualControlRegister sPredNode : crPreset) {
                    if (sPredNode == rPredNode) continue;
                    BinaryRegisterStg sPredNodeStg = getControlRegisterStg(sPredNode);
                    if (node.getReferencedComponent().getSynchronisationType() == SynchronisationType.OR) {
                        createReadArc(sPredNodeStg.m1, nodeStg.tMRs.get(rPredNode), true);
                    }
                    if (node.getReferencedComponent().getSynchronisationType() == SynchronisationType.AND) {
                        createReadArc(sPredNodeStg.m1, nodeStg.fMRs.get(rPredNode), true);
                    }
                }
            }
            createReadArc(rPredNodeStg.m0, nodeStg.tMF, false);
            createReadArc(rPredNodeStg.m0, nodeStg.fMF, false);
        }
        for (VisualPushRegister rPredNode : dfs.getRPreset(node, VisualPushRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getPushRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.tM1, nodeStg.tMRs.values(), true);
            createReadArcs(rPredNodeStg.tM1, nodeStg.fMRs.values(), true);
            createReadArc(rPredNodeStg.tM0, nodeStg.tMF, false);
            createReadArc(rPredNodeStg.tM0, nodeStg.fMF, false);
        }
        for (VisualPopRegister rPredNode : dfs.getRPreset(node, VisualPopRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getPopRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.m1, nodeStg.tMRs.values(), true);
            createReadArcs(rPredNodeStg.m1, nodeStg.fMRs.values(), true);
            createReadArc(rPredNodeStg.m0, nodeStg.tMF, false);
            createReadArc(rPredNodeStg.m0, nodeStg.fMF, false);
        }
        // R-postset
        for (VisualRegister rSuccNode : dfs.getRPostset(node, VisualRegister.class)) {
            RegisterStg rSuccNodeStg = getRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.tMF, false);
            createReadArc(rSuccNodeStg.m1, nodeStg.fMF, false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.fMRs.values(), false);
        }
        for (VisualControlRegister rSuccNode : dfs.getRPostset(node, VisualControlRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getControlRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.tMF, false);
            createReadArc(rSuccNodeStg.m1, nodeStg.fMF, false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.fMRs.values(), false);
        }
        for (VisualPushRegister rSuccNode : dfs.getRPostset(node, VisualPushRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getPushRegisterStg(rSuccNode);
            Connection connection = dfs.getConnection(node, rSuccNode);
            if ((connection instanceof VisualControlConnection)
                    && ((VisualControlConnection) connection).getReferencedConnection().isInverting()) {

                createReadArc(rSuccNodeStg.tM1, nodeStg.fMF, false);
                createReadArc(rSuccNodeStg.fM1, nodeStg.tMF, false);
            } else {
                createReadArc(rSuccNodeStg.tM1, nodeStg.tMF, false);
                createReadArc(rSuccNodeStg.fM1, nodeStg.fMF, false);
            }
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.fMRs.values(), false);
        }
        for (VisualPopRegister rSuccNode : dfs.getRPostset(node, VisualPopRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getPopRegisterStg(rSuccNode);
            Connection connection = dfs.getConnection(node, rSuccNode);
            if ((connection instanceof VisualControlConnection)
                    && ((VisualControlConnection) connection).getReferencedConnection().isInverting()) {

                createReadArc(rSuccNodeStg.tM1, nodeStg.fMF, false);
                createReadArc(rSuccNodeStg.fM1, nodeStg.tMF, false);
            } else {
                createReadArc(rSuccNodeStg.tM1, nodeStg.tMF, false);
                createReadArc(rSuccNodeStg.fM1, nodeStg.fMF, false);
            }
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.fMRs.values(), false);
        }
    }

    public BinaryRegisterStg getControlRegisterStg(VisualControlRegister node) {
        return (controlRegisterMap == null) ? null : controlRegisterMap.get(node);
    }

    public void putControlRegisterStg(VisualControlRegister node, BinaryRegisterStg nodeStg) {
        if (controlRegisterMap == null) {
            controlRegisterMap = new HashMap<>();
        }
        controlRegisterMap.put(node, nodeStg);
    }

    private BinaryRegisterStg generatePushRegisterStg(VisualPushRegister node) throws InvalidConnectionException {
        return generateBinaryRegisterSTG(node, false, false);
    }

    private void connectPushRegisterStg(VisualPushRegister node) throws InvalidConnectionException {
        VisualDfs dfs = getDfsModel();
        BinaryRegisterStg nodeStg = getPushRegisterStg(node);
        // Preset
        for (VisualLogic predNode : dfs.getPreset(node, VisualLogic.class)) {
            LogicStg predNodeStg = getLogicStg(predNode);
            createReadArcs(predNodeStg.c1, nodeStg.tMRs.values(), true);
            createReadArcs(predNodeStg.c1, nodeStg.fMRs.values(), true);
            createReadArc(predNodeStg.c0, nodeStg.tMF, false);
            createReadArc(predNodeStg.c0, nodeStg.fMF, false);
        }
        // R-preset
        for (VisualRegister rPredNode : dfs.getRPreset(node, VisualRegister.class)) {
            RegisterStg rPredNodeStg = getRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.m1, nodeStg.tMRs.values(), true);
            createReadArcs(rPredNodeStg.m1, nodeStg.fMRs.values(), true);
            createReadArc(rPredNodeStg.m0, nodeStg.tMF, false);
            createReadArc(rPredNodeStg.m0, nodeStg.fMF, false);
        }
        for (VisualControlRegister rPredNode : dfs.getRPreset(node, VisualControlRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getControlRegisterStg(rPredNode);
            Connection connection = dfs.getConnection(rPredNode, node);
            if ((connection instanceof VisualControlConnection)
                    && ((VisualControlConnection) connection).getReferencedConnection().isInverting()) {

                createReadArc(rPredNodeStg.tM1, nodeStg.fMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.fM1, nodeStg.tMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.tM0, nodeStg.fMF, false);
                createReadArc(rPredNodeStg.fM0, nodeStg.tMF, false);
            } else {
                createReadArc(rPredNodeStg.tM1, nodeStg.tMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.fM1, nodeStg.fMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.tM0, nodeStg.tMF, false);
                createReadArc(rPredNodeStg.fM0, nodeStg.fMF, false);
            }
        }
        for (VisualPushRegister rPredNode : dfs.getRPreset(node, VisualPushRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getPushRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.tM1, nodeStg.tMRs.values(), true);
            createReadArcs(rPredNodeStg.tM1, nodeStg.fMRs.values(), true);
            createReadArc(rPredNodeStg.tM0, nodeStg.tMF, false);
            createReadArc(rPredNodeStg.tM0, nodeStg.fMF, false);
        }
        for (VisualPopRegister rPredNode : dfs.getRPreset(node, VisualPopRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getPopRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.m1, nodeStg.tMRs.values(), true);
            createReadArcs(rPredNodeStg.m1, nodeStg.fMRs.values(), true);
            createReadArc(rPredNodeStg.m0, nodeStg.tMF, false);
            createReadArc(rPredNodeStg.m0, nodeStg.fMF, false);
        }
        // R-postset
        for (VisualRegister rSuccNode : dfs.getRPostset(node, VisualRegister.class)) {
            RegisterStg rSuccNodeStg = getRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.tMF, false); // register m1 in R-postset is read only by tMF
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false); // register m0 in R-postset is read only by tMR
        }
        for (VisualControlRegister rSuccNode : dfs.getRPostset(node, VisualControlRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getControlRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.tMF, false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
        }
        for (VisualPushRegister rSuccNode : dfs.getRPostset(node, VisualPushRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getPushRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.tMF, false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
        }
        for (VisualPopRegister rSuccNode : dfs.getRPostset(node, VisualPopRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getPopRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.tM1, nodeStg.tMF, false); // pop tM1 in R-postset is read only by tMF
            createReadArcs(rSuccNodeStg.tM0, nodeStg.tMRs.values(), false); // pop tM0 in R-postset is read only by tMR
        }
    }

    public BinaryRegisterStg getPushRegisterStg(VisualPushRegister node) {
        return (pushRegisterMap == null) ? null : pushRegisterMap.get(node);
    }

    public void putPushRegisterStg(VisualPushRegister node, BinaryRegisterStg nodeStg) {
        if (pushRegisterMap == null) {
            pushRegisterMap = new HashMap<>();
        }
        pushRegisterMap.put(node, nodeStg);
    }

    private BinaryRegisterStg generatePopRegisterStg(VisualPopRegister node) throws InvalidConnectionException {
        return generateBinaryRegisterSTG(node, false, false);
    }

    private void connectPopRegisterStg(VisualPopRegister node) throws InvalidConnectionException {
        VisualDfs dfs = getDfsModel();
        BinaryRegisterStg nodeStg = getPopRegisterStg(node);
        // Preset
        for (VisualLogic predNode : dfs.getPreset(node, VisualLogic.class)) {
            LogicStg predNodeStg = getLogicStg(predNode);
            createReadArcs(predNodeStg.c1, nodeStg.tMRs.values(), true);
            createReadArc(predNodeStg.c0, nodeStg.tMF, false);
        }
        // R-preset
        for (VisualRegister rPredNode : dfs.getRPreset(node, VisualRegister.class)) {
            RegisterStg rPredNodeStg = getRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.m1, nodeStg.tMRs.values(), true);
            createReadArc(rPredNodeStg.m0, nodeStg.tMF, false);
        }
        for (VisualControlRegister rPredNode : dfs.getRPreset(node, VisualControlRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getControlRegisterStg(rPredNode);
            Connection connection = dfs.getConnection(rPredNode, node);
            if ((connection instanceof VisualControlConnection)
                    && ((VisualControlConnection) connection).getReferencedConnection().isInverting()) {

                createReadArc(rPredNodeStg.tM1, nodeStg.fMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.fM1, nodeStg.tMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.tM0, nodeStg.fMF, false);
                createReadArc(rPredNodeStg.fM0, nodeStg.tMF, false);
            } else {
                createReadArc(rPredNodeStg.tM1, nodeStg.tMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.fM1, nodeStg.fMRs.get(rPredNode), true);
                createReadArc(rPredNodeStg.tM0, nodeStg.tMF, false);
                createReadArc(rPredNodeStg.fM0, nodeStg.fMF, false);
            }
        }
        for (VisualPushRegister rPredNode : dfs.getRPreset(node, VisualPushRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getPushRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.tM1, nodeStg.tMRs.values(), true);
            createReadArc(rPredNodeStg.tM0, nodeStg.tMF, false);
        }
        for (VisualPopRegister rPredNode : dfs.getRPreset(node, VisualPopRegister.class)) {
            BinaryRegisterStg rPredNodeStg = getPopRegisterStg(rPredNode);
            createReadArcs(rPredNodeStg.m1, nodeStg.tMRs.values(), true);
            createReadArc(rPredNodeStg.m0, nodeStg.tMF, false);
        }
        // R-postset
        for (VisualRegister rSuccNode : dfs.getRPostset(node, VisualRegister.class)) {
            RegisterStg rSuccNodeStg = getRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.tMF, false);
            createReadArc(rSuccNodeStg.m1, nodeStg.fMF, false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.fMRs.values(), false);
        }
        for (VisualControlRegister rSuccNode : dfs.getRPostset(node, VisualControlRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getControlRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.tMF, false);
            createReadArc(rSuccNodeStg.m1, nodeStg.fMF, false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.fMRs.values(), false);
        }
        for (VisualPushRegister rSuccNode : dfs.getRPostset(node, VisualPushRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getPushRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.m1, nodeStg.tMF, false);
            createReadArc(rSuccNodeStg.m1, nodeStg.fMF, false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.tMRs.values(), false);
            createReadArcs(rSuccNodeStg.m0, nodeStg.fMRs.values(), false);
        }
        for (VisualPopRegister rSuccNode : dfs.getRPostset(node, VisualPopRegister.class)) {
            BinaryRegisterStg rSuccNodeStg = getPopRegisterStg(rSuccNode);
            createReadArc(rSuccNodeStg.tM1, nodeStg.tMF, false);
            createReadArc(rSuccNodeStg.tM1, nodeStg.fMF, false);
            createReadArcs(rSuccNodeStg.tM0, nodeStg.tMRs.values(), false);
            createReadArcs(rSuccNodeStg.tM0, nodeStg.fMRs.values(), false);
        }
    }

    public BinaryRegisterStg getPopRegisterStg(VisualPopRegister node) {
        return (popRegisterMap == null) ? null : popRegisterMap.get(node);
    }

    public void putPopRegisterStg(VisualPopRegister node, BinaryRegisterStg nodeStg) {
        if (popRegisterMap == null) {
            popRegisterMap = new HashMap<>();
        }
        popRegisterMap.put(node, nodeStg);
    }

    public boolean isRelated(Node highLevelNode, Node node) {
        NodeStg nodeStg = null;
        if (highLevelNode instanceof VisualLogic) {
            nodeStg = getLogicStg((VisualLogic) highLevelNode);
        } else if (highLevelNode instanceof VisualRegister) {
            nodeStg = getRegisterStg((VisualRegister) highLevelNode);
        } else if (highLevelNode instanceof VisualCounterflowLogic) {
            nodeStg = getCounterflowLogicStg((VisualCounterflowLogic) highLevelNode);
        } else if (highLevelNode instanceof VisualCounterflowRegister) {
            nodeStg = getCounterflowRegisterStg((VisualCounterflowRegister) highLevelNode);
        } else if (highLevelNode instanceof VisualControlRegister) {
            nodeStg = getControlRegisterStg((VisualControlRegister) highLevelNode);
        } else if (highLevelNode instanceof VisualPushRegister) {
            nodeStg = getPushRegisterStg((VisualPushRegister) highLevelNode);
        } else if (highLevelNode instanceof VisualPopRegister) {
            nodeStg = getPopRegisterStg((VisualPopRegister) highLevelNode);
        }
        return (nodeStg != null) && nodeStg.contains(node);
    }

    public static String getLogicStgNodeReference(String ref, Boolean level) {
        return getStgNodeReference(ref, C_NAME_PREFIX, level);
    }

    public static String getRegisterStgNodeReference(String ref, Boolean level) {
        return getStgNodeReference(ref, M_NAME_PREFIX, level);
    }

    public static String getCounterflowLogicStgNodeReference(String ref, Boolean forward, Boolean level) {
        String prefix = forward ? FW_C_NAME_PREFIX : BW_C_NAME_PREFIX;
        return getStgNodeReference(ref, prefix, level);
    }

    public static String getCounterflowRegisterStgNodeReference(String ref, boolean or, Boolean level) {
        String prefix = or ? OR_M_NAME_PREFIX : AND_M_NAME_PREFIX;
        return getStgNodeReference(ref, prefix, level);
    }

    public static String getBinaryRegisterStgNodeReference(String ref, Boolean value, Boolean level) {
        String prefix = value == null ? M_NAME_PREFIX : value ? TRUE_M_NAME_PREFIX : FALSE_M_NAME_PREFIX;
        return getStgNodeReference(ref, prefix, level);
    }

    private static String getStgNodeReference(String ref, String prefix, Boolean level) {
        String suffix = level == null ? "" : level ? StgSettings.getHighLevelSuffix() : StgSettings.getLowLevelSuffix();
        String parentRef = NamespaceHelper.getParentReference(ref);
        String name = NamespaceHelper.getReferenceName(ref);
        return NamespaceHelper.getReference(parentRef, prefix + name + suffix);
    }

}
