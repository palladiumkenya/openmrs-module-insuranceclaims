package org.openmrs.module.insuranceclaims.api.client;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.exceptions.FHIRException;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;

import java.net.URISyntaxException;

public interface ClaimHttpRequest {

    ClaimResponse sendClaimRequest(String resourceUrl, InsuranceClaim insuranceClaim) throws URISyntaxException, FHIRException;

    Claim getClaimRequest(String resourceUrl, String claimCode) throws URISyntaxException;

    Bundle sendClaimBundleRequest(String resourceUrl, InsuranceClaim insuranceClaim) throws URISyntaxException, FHIRException;

    ClaimResponse getClaimResponse(String baseUrl, String claimCode) throws URISyntaxException;
}
