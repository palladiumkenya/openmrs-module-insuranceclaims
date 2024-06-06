package org.openmrs.module.insuranceclaims.api.service.fhir;

// import org.hl7.fhir.dstu3.model.EligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;

public interface FHIREligibilityService {
    CoverageEligibilityRequest generateEligibilityRequest(String policyId);
}