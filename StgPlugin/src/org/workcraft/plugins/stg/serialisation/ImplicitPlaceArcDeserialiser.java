package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class ImplicitPlaceArcDeserialiser implements CustomXMLDeserialiser<VisualImplicitPlaceArc> {
    @Override
    public String getClassName() {
        return VisualImplicitPlaceArc.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, VisualImplicitPlaceArc instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        instance.setImplicitPlaceArcDependencies(
                (MathConnection) externalReferenceResolver.getObject(element.getAttribute("refCon1")),
                (MathConnection) externalReferenceResolver.getObject(element.getAttribute("refCon2")),
                (StgPlace) externalReferenceResolver.getObject(element.getAttribute("refPlace"))
        );
    }

    @Override
    public VisualImplicitPlaceArc createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new VisualImplicitPlaceArc();
    }

    @Override
    public void initInstance(Element element, VisualImplicitPlaceArc instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {

    }

}