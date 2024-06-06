package org.openmrs.module.insuranceclaims.api.client.impl;

// import org.hl7.fhir.dstu3.model.EligibilityRequest;
// import org.hl7.fhir.dstu3.model.EligibilityResponse;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.openmrs.module.insuranceclaims.api.client.EligibilityHttpRequest;

import java.net.URISyntaxException;

public class EligibilityHttpRequestImpl implements EligibilityHttpRequest {

    private FhirRequestClient client;

    @Override
    public CoverageEligibilityResponse sendEligibilityRequest(String resourceUrl, CoverageEligibilityRequest request) throws URISyntaxException {
        String url = resourceUrl + "/";
        CoverageEligibilityResponse response = client.postObject(url, request, CoverageEligibilityResponse.class);

        return response;
    }

    public void setClient(FhirRequestClient client) {
        this.client = client;
    }
}
