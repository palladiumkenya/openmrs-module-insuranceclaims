package org.openmrs.module.insuranceclaims;

import org.openmrs.Obs;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.FormService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
// import org.openmrs.event.Event;
// import org.openmrs.event.EventListener;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
// import org.openmrs.module.htmlformentry.HtmlFormEntryService;
// import org.openmrs.module.htmlformentryui.HtmlFormUtil;
import org.openmrs.module.insuranceclaims.activator.concept.ModuleConceptSetup;
import org.openmrs.module.insuranceclaims.util.ConstantValues;
// import org.openmrs.ui.framework.resource.ResourceFactory;

import java.io.IOException;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.CONSUMED_ITEMS_FORM_UUID;

import org.openmrs.module.ModuleActivator;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
// public class InsuranceClaimsActivator implements ModuleActivator {
		
// 	/**
// 	 * @see ModuleActivator#willRefreshContext()
// 	 */
// 	public void willRefreshContext() {
// 		System.err.println("Refreshing Insurance Claims Module");
// 	}
	
// 	/**
// 	 * @see ModuleActivator#contextRefreshed()
// 	 */
// 	public void contextRefreshed() {
// 		System.err.println("Insurance Claims Module refreshed");
// 	}
	
// 	/**
// 	 * @see ModuleActivator#willStart()
// 	 */
// 	public void willStart() {
// 		System.err.println("Starting Insurance Claims Module");
// 	}
	
// 	/**
// 	 * @see ModuleActivator#started()
// 	 */
// 	public void started() {
// 		System.err.println("Insurance Claims Module started");
// 	}
	
// 	/**
// 	 * @see ModuleActivator#willStop()
// 	 */
// 	public void willStop() {
// 		System.err.println("Stopping Insurance Claims Module");
// 	}
	
// 	/**
// 	 * @see ModuleActivator#stopped()
// 	 */
// 	public void stopped() {
// 		System.err.println("Insurance Claims Module stopped");
// 	}
		
// }

/**
 * Contains the logic that is run every time this module is either started or stopped
 */
public class InsuranceClaimsActivator extends BaseModuleActivator implements DaemonTokenAware {

	private static final String MODULE_START_MESSAGE = "Started Insurance Claims";
	private static final String MODULE_STOP_MESSAGE  = "Stopped Insurance Claims";

	private static final String PATH_TO_CONSUMED_ITEM_FORM_TEMPLATE = "insuranceclaims:htmlforms/consumedItemFormTemplate.xml";

	private DaemonToken daemonToken;

	// private EventListener eventListener;

	/**
	 * @see #started()
	 */
	@Override
	public void started() {
		System.err.println("Insurance Claims Module started");
		addConcepts();

		// HtmlFormEntryService service = Context.getService(HtmlFormEntryService.class);
		// if (service.getHtmlFormByUuid(CONSUMED_ITEMS_FORM_UUID) == null) {
		// 	try {
		// 		setupHtmlForms();
		// 	} catch (Exception e) {
		// 		System.err.println("Failed to load consumed item form. Caused by:  " + e.toString());
		// 	}
		// }

		createInsureNumberAttribute();

		// eventListener = getItemConsumedListener();
		// Event.subscribe(Obs.class, Event.Action.CREATED.name(), eventListener);
		System.err.println(MODULE_START_MESSAGE);
	}

	/**
	 * @see #stopped()
	 */
	@Override
	public void stopped() {
		System.err.println("Insurance Claims Module stopped");
		// Event.unsubscribe(Obs.class, Event.Action.CREATED, eventListener);
		System.err.println(MODULE_STOP_MESSAGE);
	}

	@Override
	public void setDaemonToken(DaemonToken token) {
		daemonToken = token;
	}

	private void addConcepts() {
		Context.getService(ModuleConceptSetup.class).createConcepts();
	}

	// private EventListener getItemConsumedListener() {
	// 	return new ItemConsumedEventListener(daemonToken);
	// }

	// private void setupHtmlForms() throws IOException {
	// 	ResourceFactory resourceFactory = ResourceFactory.getInstance();
	// 	FormService formService = Context.getFormService();
	// 	HtmlFormEntryService htmlFormEntryService = Context.getService(HtmlFormEntryService.class);

	// 	HtmlFormUtil.getHtmlFormFromUiResource(resourceFactory, formService, htmlFormEntryService, PATH_TO_CONSUMED_ITEM_FORM_TEMPLATE);
	// }

	private void createInsureNumberAttribute() {
		PersonAttributeType attributeType = new PersonAttributeType();
		attributeType.setName(ConstantValues.POLICY_NUMBER_ATTRIBUTE_TYPE_NAME);
		attributeType.setFormat(ConstantValues.POLICY_NUMBER_ATTRIBUTE_TYPE_FORMAT);
		attributeType.setDescription(ConstantValues.POLICY_NUMBER_ATTRIBUTE_TYPE_DESCRIPTION);
		attributeType.setUuid(ConstantValues.POLICY_NUMBER_ATTRIBUTE_TYPE_UUID);
		createPersonAttributeTypeIfNotExists(attributeType);
	}

	private void createPersonAttributeTypeIfNotExists(PersonAttributeType attributeType) {
		PersonService personService = Context.getPersonService();
		PersonAttributeType actual = personService.getPersonAttributeTypeByUuid(attributeType.getUuid());
		if (actual == null) {
			personService.savePersonAttributeType(attributeType);
		}
	}

	@Override
	public void willRefreshContext() {
		System.err.println("Insurance Claims Module refreshing context");
	}
 
	@Override
	public void willStart() {
		System.err.println("Insurance Claims Module starting");
	}
 
	@Override
	public void willStop() {
		System.err.println("Insurance Claims Module stopping");
	}

	@Override
	public void contextRefreshed() {
		System.err.println("Insurance Claims Module refreshing context");
	}
}
