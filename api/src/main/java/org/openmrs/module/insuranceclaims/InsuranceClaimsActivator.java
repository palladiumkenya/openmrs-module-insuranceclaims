package org.openmrs.module.insuranceclaims;

import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.insuranceclaims.activator.concept.ModuleConceptSetup;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.GeneralUtil;
import org.openmrs.module.insuranceclaims.util.ConstantValues;

/**
 * Contains the logic that is run every time this module is either started or stopped
 */
public class InsuranceClaimsActivator extends BaseModuleActivator implements DaemonTokenAware {

	private static final String MODULE_START_MESSAGE = "Started Insurance Claims";
	private static final String MODULE_STOP_MESSAGE  = "Stopped Insurance Claims";

	private static DaemonToken daemonToken;

	/**
	 * @see #started()
	 */
	@Override
	public void started() {
		System.err.println("Insurance Claims Module started: " + GeneralUtil.getCurrentDateTime());
		addConcepts();

		createInsureNumberAttribute();

		System.err.println(MODULE_START_MESSAGE);
	}

	/**
	 * @see #stopped()
	 */
	@Override
	public void stopped() {
		System.err.println("Insurance Claims Module: stopped module: " + GeneralUtil.getCurrentDateTime());
		System.err.println(MODULE_STOP_MESSAGE);
	}

	@Override
	public void setDaemonToken(DaemonToken token) {
		System.err.println("Insurance Claims Module: Got daemon token as: " + token + " : "  + GeneralUtil.getCurrentDateTime());
		// InsuranceClaimsActivator.daemonToken = token;
		daemonToken = token;
	}

	private void addConcepts() {
		Context.getService(ModuleConceptSetup.class).createConcepts();
	}

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
		System.err.println("Insurance Claims Module: Will refresh context: " + GeneralUtil.getCurrentDateTime());
	}
 
	@Override
	public void willStart() {
		System.err.println("Insurance Claims Module: Will start: " + GeneralUtil.getCurrentDateTime());
	}
 
	@Override
	public void willStop() {
		System.err.println("Insurance Claims Module: Will stop: " + GeneralUtil.getCurrentDateTime());
	}

	@Override
	public void contextRefreshed() {
		System.err.println("Insurance Claims Module: finished refreshing context: " + GeneralUtil.getCurrentDateTime());
	}
	
	public static DaemonToken getDaemonToken() {
		return daemonToken;
	}
}
