/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.insuranceclaims.util;

import org.openmrs.LocationAttributeType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.client.ClientConstants;

public class ClaimsUtils {

	public static PatientIdentifierType getNUPIIdentifierType() {
		String NUPIIdentifierType = Context.getAdministrationService()
		        .getGlobalPropertyValue(ClientConstants.NATIONAL_UNIQUE_PATIENT_NUMBER_UUID, "");
		return Context.getPatientService().getPatientIdentifierTypeByUuid(NUPIIdentifierType);
	}

	public static PatientIdentifierType getCRIDIdentifierType() {
		String CRIDIdentifierType = Context.getAdministrationService()
		        .getGlobalPropertyValue(ClientConstants.SOCIAL_HEALTH_AUTHORITY_IDENTIFICATION_NUMBER_UUID, "");
		return Context.getPatientService().getPatientIdentifierTypeByUuid(CRIDIdentifierType);
	}

	public static LocationAttributeType getMFLCODELocationAttributeType() {
		String MFLCODELocationAttributeType = Context.getAdministrationService()
		        .getGlobalPropertyValue(ClientConstants.INTEROP_MFLCODE_LOCATION_ATTRIBUTE_TYPE_UUID, "");
		return Context.getLocationService().getLocationAttributeTypeByUuid(MFLCODELocationAttributeType);
	}

	public static ProviderAttributeType getProviderAttributeType() {
		String providerAttributeType = Context.getAdministrationService()
		        .getGlobalPropertyValue(ClientConstants.INTEROP_PROVIDER_ATTRIBUTE_TYPE_UUID, "");
		return Context.getProviderService().getProviderAttributeTypeByUuid(providerAttributeType);

	}
	public static String getCRSystemUrlConfiguration() {
		return Context.getAdministrationService().getGlobalPropertyValue(ClientConstants.CR_SYSTEM_URL, "");
	}
	public static String getSystemUrlConfiguration() {
		return Context.getAdministrationService().getGlobalPropertyValue(ClientConstants.SYSTEM_URL, "");
	}

	public static String getKhmflSystemUrlConfiguration() {
		return Context.getAdministrationService().getGlobalPropertyValue(ClientConstants.KMHFL_SYSTEM_URL, "");
	}

}
