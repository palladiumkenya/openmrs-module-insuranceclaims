package org.openmrs.module.insuranceclaims.api.service.fhir;

import org.hl7.fhir.r4.model.CoverageEligibilityRequest;

public interface FHIREligibilityService {
    CoverageEligibilityRequest generateEligibilityRequest(String policyId);
}