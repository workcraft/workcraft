package org.workcraft.dom.converters;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.utils.Hierarchy;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractModelConverter<S extends VisualModel, T extends VisualModel>
        implements ModelConverter<S, T> {

    private final S srcModel;
    private final T dstModel;
    private final Map<VisualNode, VisualNode> srcToDstNodes = new HashMap<>();
    private final Map<String, Container> refToDstPage = new HashMap<>();

    public AbstractModelConverter(S srcModel, T dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        preprocessing();
        convertTitle();
        convertPages();
        convertComponents();
        convertReplicas();
        convertGroups();
        // Connections must be converted the last as their shapes change with node relocation.
        convertConnections();
        postprocessing();
    }

    @Override
    public S getSrcModel() {
        return srcModel;
    }

    @Override
    public T getDstModel() {
        return dstModel;
    }

    @Override
    public String convertTitle(String title) {
        return title;
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

    private void convertTitle() {
        dstModel.setTitle(convertTitle(srcModel.getTitle()));
    }

    private void convertPages() {
        NamespaceHelper.copyPageStructure(getSrcModel(), getDstModel());
        HashMap<String, Container> tmp = NamespaceHelper.getRefToPageMapping(getDstModel());
        refToDstPage.putAll(tmp);
        for (VisualPage srcPage: Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), VisualPage.class)) {
            VisualPage dstPage = convertPage(srcPage);
            positionNode(srcPage, dstPage);
            copyStyle(srcPage, dstPage);
            putSrcToDstNode(srcPage, dstPage);
        }
    }

    private void convertComponents() {
        for (Class<? extends MathNode> srcMathNodeClass: getComponentClassMap().keySet()) {
            Class<? extends VisualComponent> srcVisualComponentClass = getVisualComponentClass(srcMathNodeClass);
            for (VisualComponent srcComponent: Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), srcVisualComponentClass)) {
                VisualComponent dstComponent = convertComponent(srcComponent);
                positionNode(srcComponent, dstComponent);
                copyStyle(srcComponent, dstComponent);
                putSrcToDstNode(srcComponent, dstComponent);
            }
        }
    }

    private void convertReplicas() {
        for (Class<? extends VisualReplica> srcVisualReplicaClass: getReplicaClassMap().keySet()) {
            for (VisualReplica srcReplica: Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), srcVisualReplicaClass)) {
                VisualReplica dstReplica = convertReplica(srcReplica);
                positionNode(srcReplica, dstReplica);
                copyStyle(srcReplica, dstReplica);
                putSrcToDstNode(srcReplica, dstReplica);
            }
        }
    }

    private void convertConnections() {
        for (VisualConnection srcConnection : Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), VisualConnection.class)) {
            VisualConnection dstConnection = convertConnection(srcConnection);
            shapeConnection(srcConnection, dstConnection);
            copyStyle(srcConnection, dstConnection);
            putSrcToDstNode(srcConnection, dstConnection);
        }
    }

    private void convertGroups() {
        for (VisualGroup srcGroup: Hierarchy.getDescendantsOfType(getSrcModel().getRoot(), VisualGroup.class)) {
            VisualGroup dstGroup = convertGroup(srcGroup);
            positionNode(srcGroup, dstGroup);
            copyStyle(srcGroup, dstGroup);
            putSrcToDstNode(srcGroup, dstGroup);
        }
    }

}
