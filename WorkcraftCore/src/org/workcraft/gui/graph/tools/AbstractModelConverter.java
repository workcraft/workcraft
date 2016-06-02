package org.workcraft.gui.graph.tools;

import java.util.HashMap;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Hierarchy;

public abstract class AbstractModelConverter<TSrcModel extends VisualModel, TDstModel extends VisualModel> implements ModelConverter<TSrcModel, TDstModel> {
    private final TSrcModel srcModel;
    private final TDstModel dstModel;
    private final HashMap<VisualNode, VisualNode> srcToDstNodes = new HashMap<>();
    private final HashMap<String, Container> refToDstPage = new HashMap<>();

    public AbstractModelConverter(TSrcModel srcModel, TDstModel dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        preprocessing();
        convertPages();
        convertComponents();
        convertReplicas();
        convertGroups();
        // Connections must be converted the last as their shapes change with node relocation.
        convertConnections();
        postprocessing();
    }

    @Override
    public TSrcModel getSrcModel() {
        return srcModel;
    }

    @Override
    public TDstModel getDstModel() {
        return dstModel;
    }

    private void putSrcToDstNode(VisualNode srcNode, VisualNode dstNode) {
        if ((srcNode != null) && (dstNode != null)) {
            srcToDstNodes.put(srcNode, dstNode);
        }
    }

    @Override
    public VisualNode getSrcToDstNode(VisualNode srcNode) {
        return srcToDstNodes.get(srcNode);
    }

    @Override
    public Container getRefToDstPage(String ref) {
        return refToDstPage.get(ref);
    }

    public Class<? extends VisualComponent> getVisualComponentClass(Class<? extends MathNode> mathNodeClass) {
        VisualClass visualClassAnnotation = mathNodeClass.getAnnotation(VisualClass.class);
        Class<?> visualClass = visualClassAnnotation.value();
        return visualClass.asSubclass(VisualComponent.class);
    }

    private void convertPages() {
        NamespaceHelper.copyPageStructure(getSrcModel(), getDstModel());
        HashMap<String, Container> tmp = NamespaceHelper.getRefToPageMapping(getDstModel());
        refToDstPage.putAll(tmp);
        for (VisualPage srcPage: Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), VisualPage.class)) {
            VisualPage dstPage = convertPage(srcPage);
            putSrcToDstNode(srcPage, dstPage);
        }
    }

    private void convertComponents() {
        for (Class<? extends MathNode> srcMathNodeClass: getComponentClassMap().keySet()) {
            Class<? extends VisualComponent> srcVisualComponentClass = getVisualComponentClass(srcMathNodeClass);
            for (VisualComponent srcComponent: Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), srcVisualComponentClass)) {
                VisualComponent dstComponent = convertComponent(srcComponent);
                putSrcToDstNode(srcComponent, dstComponent);
            }
        }
    }

    private void convertReplicas() {
        for (Class<? extends VisualReplica> srcVisualReplicaClass: getReplicaClassMap().keySet()) {
            for (VisualReplica srcReplica: Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), srcVisualReplicaClass)) {
                VisualReplica dstReplica = convertReplica(srcReplica);
                putSrcToDstNode(srcReplica, dstReplica);
            }
        }
    }

    private void convertConnections() {
        for (VisualConnection srcConnection : Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), VisualConnection.class)) {
            VisualConnection dstConnection = convertConnection(srcConnection);
            putSrcToDstNode(srcConnection, dstConnection);
        }
    }

    private void convertGroups() {
        for (VisualGroup srcGroup: Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), VisualGroup.class)) {
            VisualGroup dstGroup = convertGroup(srcGroup);
            putSrcToDstNode(srcGroup, dstGroup);
        }
    }

}
