package org.workcraft.plugins.stg.serialisation;

import org.junit.jupiter.api.Test;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.PluginProvider;
import org.workcraft.plugins.builtin.commands.RandomLayoutCommand;
import org.workcraft.plugins.builtin.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.builtin.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.serialisation.DeserialisationResult;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.shared.DataAccumulator;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

class StgSerialisationTests {

    @Test
    void simpleVisualSaveLoadWithImplicitArcs() throws SerialisationException, DeserialisationException {

        VisualStg stg = XMLSerialisationTestingUtils.createTestSTG3();

        RandomLayoutCommand layout = new RandomLayoutCommand();
        WorkspaceEntry we = new WorkspaceEntry();
        we.setModelEntry(new ModelEntry(new StgDescriptor(), stg));
        layout.run(we);

        // serialise
        PluginProvider mockPluginManager = XMLSerialisationTestingUtils.createMockPluginManager();

        XMLModelSerialiser serialiser = new XMLModelSerialiser(mockPluginManager);

        DataAccumulator mathData = new DataAccumulator();
        ReferenceProducer mathModelReferences = serialiser.serialise(stg.getMathModel(), mathData, null);

        DataAccumulator visualData = new DataAccumulator();
        serialiser.serialise(stg, visualData, mathModelReferences);

        System.out.println(new String(mathData.getData()));
        System.out.println("---------------");
        System.out.println(new String(visualData.getData()));

        // deserialise
        XMLModelDeserialiser deserialiser = new XMLModelDeserialiser(mockPluginManager);

        DeserialisationResult mathResult = deserialiser.deserialise(mathData.getInputStream(), null, null);
        DeserialisationResult visualResult = deserialiser.deserialise(visualData.getInputStream(), mathResult.references, mathResult.model);

        SerialisationTestingUtils.compareNodes(stg.getRoot(), visualResult.model.getRoot());
    }
}
