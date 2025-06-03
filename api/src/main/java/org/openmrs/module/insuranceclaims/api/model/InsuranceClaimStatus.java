package org.openmrs.module.insuranceclaims.api.model;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.ENUMERATION_FROM;

/**
 * The allowable {@link InsuranceClaim} statuses.
 */
public enum InsuranceClaimStatus {
	REJECTED, ENTERED, CHECKED, PROCESSED, VALUATED, APPROVED, IN_REVIEW, CLINICAL_REVIEW, SENT_FOR_PAYMENT_PROCESSING,
	SENT_TO_SURVEILLANCE, PAYMENT_COMPLETED, PAYMENT_DECLINED, QUEUED, PENDING;

	/**
	 * FHIR associates status with numbers starting from, i.e.:
	 * REJECTED - 1
	 * ENTERED - 2
	 * ...
	 * This method allows to correctly assign status to number
	 */
	public int getNumericStatus() {
		return ordinal() + ENUMERATION_FROM;
	}
}
