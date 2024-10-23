package org.openmrs.module.insuranceclaims.api.service.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.openmrs.Patient;

public interface FHIREligibilityService {
    CoverageEligibilityRequest generateEligibilityRequest(String policyId);
    Bundle generateEligibilityRequestBundle(CoverageEligibilityRequest request, Patient patient);
}
