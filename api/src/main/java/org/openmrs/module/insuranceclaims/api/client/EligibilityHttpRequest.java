package org.openmrs.module.insuranceclaims.api.client;

// import org.hl7.fhir.dstu3.model.EligibilityRequest;
// import org.hl7.fhir.dstu3.model.EligibilityResponse;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;

import java.net.URISyntaxException;

public interface EligibilityHttpRequest {
    CoverageEligibilityResponse sendEligibilityRequest(String resourceUrl, CoverageEligibilityRequest request) throws URISyntaxException;
}
