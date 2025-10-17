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

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.openmrs.GlobalProperty;
import org.openmrs.LocationAttributeType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.insuranceclaims.api.client.ClientConstants;
import org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants;

import ca.uhn.fhir.context.FhirContext;
import liquibase.hub.model.Operation.OperationStatus;

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

	/**
	 * Convert FHIR Resource to string
	 * @param theResource
	 * @return
	 */
	public static String convertFHIRObjectToString(IBaseResource theResource) {
		String ret = "";
		if(theResource != null) {
			try {
				FhirContext fhirContext = FhirContext.forR4();
				String result = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(theResource);
				return(result);
			} catch (Exception ex) {}
		}
		return(ret);
	}

	/**
	 * Convert operation output issues to a string
	 * @param issues
	 * @return
	 */
	public static String operationOutcomeIssuesToString(List<OperationOutcomeIssueComponent> issues) {
		String ret = "";
		if(issues != null) {
			return issues.stream()
				.map(issue -> String.format(
					"[severity=%s, code=%s, diagnostics=%s]",
					issue.getSeverity(),
					issue.getCode(),
					issue.getDiagnostics()
				))
				.collect(Collectors.joining(" | "));
		}
		return(ret);
    }

	/**
	 * converts a json payload of OperationOutcome to an object
	 * @param payload
	 * @return
	 */
	public static OperationOutcome getOperationOutcomeFromJson(String payload) {
		OperationOutcome operationOutcome = null;
		try {
			FhirContext ctx = FhirContext.forR4();
        	return (OperationOutcome) ctx.newJsonParser().parseResource(OperationOutcome.class, payload);
		} catch (Exception ex) {}
		return(operationOutcome);
	}

	/**
	 * Checks whether Insurance Claims Logging is enabled
	 * 
	 * @return true (Enabled) and false (Disabled)
	 */
	public static Boolean isClaimsLoggingEnabled() {
		Boolean ret = false;
		
		GlobalProperty globalLoggingEnabled = Context.getAdministrationService().getGlobalPropertyObject(
		    InsuranceClaimConstants.CLAIMS_LOGGING_ENABLED);
		String isClaimsLoggingEnabled = globalLoggingEnabled.getPropertyValue();
		
		if (isClaimsLoggingEnabled != null && isClaimsLoggingEnabled.trim().equalsIgnoreCase("true")) {
			ret = true;
		}
		
		return (ret);
	}

	/**
	 * Gets the Insurance Claims Attachments URL
	 * 
	 * @return
	 */
	public static String getClaimsAttachmentsURL() {
		String ret = "";
		
		GlobalProperty globalPostUrl = Context.getAdministrationService().getGlobalPropertyObject(
		    InsuranceClaimConstants.CLAIM_ATTACHMENTS_URL);
		String baseURL = globalPostUrl.getPropertyValue();
		
		if (baseURL == null || baseURL.trim().isEmpty()) {
			baseURL = "https://ilm-dev.dha.go.ke/fs/fs/api/v1/media/upload";
		}
		ret = baseURL.trim();
		
		return (ret);
	}

	/**
	 * Gets the Insurance Claims Attachments USERNAME
	 * 
	 * @return
	 */
	public static String getClaimsAttachmentsUsername() {
		String ret = "";
		
		GlobalProperty globalAttachmentUsername = Context.getAdministrationService().getGlobalPropertyObject(
		    InsuranceClaimConstants.CLAIM_ATTACHMENTS_USERNAME);
		String baseUsername = globalAttachmentUsername.getPropertyValue();
		
		if (baseUsername == null || baseUsername.trim().isEmpty()) {
			baseUsername = "test";
		}
		ret = baseUsername.trim();
		
		return (ret);
	}

	/**
	 * Gets the Insurance Claims Attachments PASSWORD
	 * 
	 * @return
	 */
	public static String getClaimsAttachmentsPassword() {
		String ret = "";
		
		GlobalProperty globalAttachmentPassword = Context.getAdministrationService().getGlobalPropertyObject(
		    InsuranceClaimConstants.CLAIM_ATTACHMENTS_URL);
		String basePassword = globalAttachmentPassword.getPropertyValue();
		
		if (basePassword == null || basePassword.trim().isEmpty()) {
			basePassword = "test";
		}
		ret = basePassword.trim();
		
		return (ret);
	}
}
