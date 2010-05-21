/**
 *
 */
package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.plugins.stg.STG;

/**
 * @author Dominic Wist
 * Petrify's Complex Gate Synthesis without Technology Mapping
 */
@DisplayName("Petrify: Complex Gate Synthesis")
public class ComplexGateSynthesis implements Tool {

	/* (non-Javadoc)
	 * @see org.workcraft.Tool#getSection()
	 */
	@Override
	public String getSection() {
		return "Tools";
	}

	/* (non-Javadoc)
	 * @see org.workcraft.Tool#isApplicableTo(org.workcraft.dom.Model)
	 */
	@Override
	public boolean isApplicableTo(Model model) {
		if (model instanceof STG)
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see org.workcraft.Tool#run(org.workcraft.dom.Model, org.workcraft.Framework)
	 */
	@Override
	public void run(Model model, Framework framework) {

		// call petrify asynchronous (w/o blocking the GUI) without an configuration dialog
//		framework.getTaskManager().queue(new DesiJTask(model, framework, new String[0]),
//				"Execution of DesiJ", new DecompositionResultHandler(framework, true));

	}

}
